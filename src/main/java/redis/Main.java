package redis;

import redis.protocol.RespDecoder;
import redis.protocol.RespEncoder;
import redis.protocol.RespValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(6379)) {

            System.out.println("Redis server listening on port 6379");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                Thread.ofVirtual().start(
                        () -> handleClient(clientSocket)
                );
            }
        }
    }

    private static void handleClient(Socket clientSocket) {

        System.out.println(
                "Client connected: "
                        + clientSocket.getRemoteSocketAddress()
                        + " | Thread: "
                        + Thread.currentThread()
        );

        RespDecoder decoder = new RespDecoder();
        RespEncoder encoder = new RespEncoder();

        try (
                clientSocket;
                InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream()
        ) {

            while (true) {

                RespValue request = decoder.decode(input);

                RespValue response = handleCommand(request);

                encoder.encode(response, output);

                output.flush();
            }

        } catch (IOException e) {

            System.out.println(
                    "Client disconnected: "
                            + clientSocket.getRemoteSocketAddress()
            );
        }
    }

    private static RespValue handleCommand(RespValue request) {

        if (!(request instanceof RespValue.Array array)) {
            return new RespValue.Error("ERR expected array");
        }

        if (array.values().isEmpty()) {
            return new RespValue.Error("ERR empty command");
        }

        RespValue first = array.values().get(0);

        if (!(first instanceof RespValue.BulkString commandValue)) {
            return new RespValue.Error("ERR command must be a bulk string");
        }

        String command = commandValue.value().toUpperCase();

        return switch (command) {

            case "PING" -> handlePing(array);

            case "ECHO" -> handleEcho(array);

            default -> new RespValue.Error(
                    "ERR unknown command '" + command + "'"
            );
        };
    }

    private static RespValue handlePing(RespValue.Array array) {

        if (array.values().size() == 1) {
            return new RespValue.SimpleString("PONG");
        }

        if (array.values().size() == 2
                && array.values().get(1) instanceof RespValue.BulkString message) {

            return new RespValue.BulkString(message.value());
        }

        return new RespValue.Error("ERR wrong number of arguments for 'ping'");
    }

    private static RespValue handleEcho(RespValue.Array array) {

        if (array.values().size() != 2) {
            return new RespValue.Error(
                    "ERR wrong number of arguments for 'echo'"
            );
        }

        if (!(array.values().get(1) instanceof RespValue.BulkString message)) {
            return new RespValue.Error("ERR argument must be a bulk string");
        }

        return new RespValue.BulkString(message.value());
    }
}