package redis.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.network.RedisServer;
import redis.protocol.RespDecoder;
import redis.protocol.RespEncoder;
import redis.protocol.RespValue;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedisServerIntegrationTest {

    private RedisServer server;

    @BeforeEach
    void startServer() throws Exception {

        server = new RedisServer(0);

        Thread.ofVirtual().start(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.awaitStarted();
    }

    @AfterEach
    void stopServer() throws Exception {
        server.stop();
    }

    @Test
    void multipleClientsShouldShareSameDataStore()
            throws Exception {

        try (
                Socket client1 =
                        new Socket("localhost", server.getPort());

                Socket client2 =
                        new Socket("localhost", server.getPort())
        ) {

            RespEncoder encoder1 =
                    new RespEncoder();

            RespDecoder decoder1 =
                    new RespDecoder();

            RespEncoder encoder2 =
                    new RespEncoder();

            RespDecoder decoder2 =
                    new RespDecoder();

            // Client 1:
            // SET counter 0
            sendCommand(
                    encoder1,
                    client1,
                    List.of("SET", "counter", "0"));

            assertEquals(
                    new RespValue.SimpleString("OK"),
                    decoder1.decode(client1.getInputStream()));

            // Client 2:
            // GET counter
            sendCommand(
                    encoder2,
                    client2,
                    List.of("GET", "counter"));

            assertEquals(
                    new RespValue.BulkString("0"),
                    decoder2.decode(client2.getInputStream()));

            // Client 2:
            // INCR counter
            sendCommand(
                    encoder2,
                    client2,
                    List.of("INCR", "counter"));

            assertEquals(
                    new RespValue.IntegerValue(1),
                    decoder2.decode(client2.getInputStream()));

            // Client 1 should see Client 2's change.
            sendCommand(
                    encoder1,
                    client1,
                    List.of("GET", "counter"));

            assertEquals(
                    new RespValue.BulkString("1"),
                    decoder1.decode(client1.getInputStream()));
        }
    }

    @Test
    void multipleClientsShouldIncrementSafely() throws Exception {
        final int clientCount = 20;

        try (
                Socket setupClient =
                        new Socket(
                                "localhost",
                                server.getPort())
        ) {

            RespEncoder encoder =
                    new RespEncoder();

            RespDecoder decoder =
                    new RespDecoder();

            sendCommand(
                    encoder,
                    setupClient,
                    List.of("SET", "shared", "0"));

            assertEquals(
                    new RespValue.SimpleString("OK"),
                    decoder.decode(
                            setupClient.getInputStream()));
        }

        try (
                ExecutorService executor =
                        java.util.concurrent.Executors
                                .newVirtualThreadPerTaskExecutor()
        ) {

            List<Future<Integer>> futures = new ArrayList<>();

            for (int i = 0; i < clientCount; i++) {
                futures.add(
                        executor.submit(() -> {
                            try (
                                    Socket socket =
                                            new Socket(
                                                    "localhost",
                                                    server.getPort())
                            ) {
                                RespEncoder encoder =
                                        new RespEncoder();

                                RespDecoder decoder =
                                        new RespDecoder();

                                sendCommand(
                                        encoder,
                                        socket,
                                        List.of(
                                                "INCR",
                                                "shared"));

                                RespValue response =
                                        decoder.decode(
                                                socket.getInputStream());

                                return (int)
                                        ((RespValue.IntegerValue)
                                                response).value();
                            }
                        }));
            }

            for (Future<Integer> future : futures) {
                future.get();
            }
        }

        try (
                Socket client =
                        new Socket(
                                "localhost",
                                server.getPort())
        ) {

            RespEncoder encoder = new RespEncoder();

            RespDecoder decoder = new RespDecoder();

            sendCommand(
                    encoder,
                    client,
                    List.of("GET", "shared"));

            assertEquals(
                    new RespValue.BulkString(
                            String.valueOf(clientCount)),
                    decoder.decode(
                            client.getInputStream()));
        }
    }

    private void sendCommand(
            RespEncoder encoder,
            Socket socket,
            List<String> arguments)
            throws IOException {

        List<RespValue> values =
                arguments.stream()
                        .map(RespValue.BulkString::new)
                        .map(value -> (RespValue) value)
                        .toList();

        encoder.encode(
                new RespValue.Array(values),
                socket.getOutputStream());
    }
}