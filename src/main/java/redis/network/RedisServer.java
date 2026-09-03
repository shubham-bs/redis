package redis.network;

import redis.command.CommandDispatcher;
import redis.command.CommandRequestParser;
import redis.protocol.RespDecoder;
import redis.protocol.RespEncoder;
import redis.protocol.RespValue;
import redis.storage.DataStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class RedisServer {

    private final int port;
    private final DataStore store;
    private final CommandRequestParser parser;
    private final CommandDispatcher dispatcher;

    private volatile ServerSocket serverSocket;
    private volatile boolean running;

    private final CountDownLatch started = new CountDownLatch(1);

    public RedisServer(int port) {
        this.port = port;
        this.store = new DataStore();
        this.parser = new CommandRequestParser();
        this.dispatcher = new CommandDispatcher();
    }

    public void start() throws IOException {

        try (ServerSocket socket = new ServerSocket(port)) {

            serverSocket = socket;
            running = true;

            System.out.println("Redis server listening on port " + socket.getLocalPort());

            started.countDown();

            while (running) {
                try {
                    Socket client = socket.accept();
                    Thread.ofVirtual().start(() -> handleClient(client));
                } catch (IOException e) {
                    if (running) throw e;
                }
            }
        }
    }

    public void awaitStarted() throws InterruptedException {
        started.await();
    }

    public int getPort() {
        ServerSocket socket = serverSocket;
        if (socket == null) throw new IllegalStateException("Server has not started");

        return socket.getLocalPort();
    }

    public void stop() throws IOException {
        running = false;
        ServerSocket socket = serverSocket;
        if (socket != null) socket.close();
    }

    private void handleClient(Socket socket) {

        System.out.println("Client connected: " + socket.getRemoteSocketAddress() + " | Thread: " + Thread.currentThread());

        try (socket) {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            RespDecoder decoder = new RespDecoder();
            RespEncoder encoder = new RespEncoder();

            while (true) {
                RespValue value = decoder.decode(input);

                if (value == null) break;

                var request = parser.parse(value);

                var response = dispatcher.dispatch(store, request);

                encoder.encode(response, output);
            }

        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());

        } catch (IllegalArgumentException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}