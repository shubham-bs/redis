package redis.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.network.RedisServer;
import redis.protocol.RespDecoder;
import redis.protocol.RespEncoder;
import redis.protocol.RespValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PipeliningIntegrationTest {

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
    void shouldProcessMultipleCommandsInOnePipeline() throws Exception {
        try (
                Socket socket =
                        new Socket(
                                "localhost",
                                server.getPort())
        ) {

            RespEncoder encoder = new RespEncoder();

            RespDecoder decoder = new RespDecoder();

            /*
             * This represents a client sending a pipeline:
             *
             * SET a 1
             * SET b 2
             * INCR a
             * GET a
             * GET b
             *
             * No response is read between commands.
             */

            ByteArrayOutputStream pipeline = new ByteArrayOutputStream();

            encodeCommand(
                    encoder,
                    pipeline,
                    List.of("SET", "a", "1"));

            encodeCommand(
                    encoder,
                    pipeline,
                    List.of("SET", "b", "2"));

            encodeCommand(
                    encoder,
                    pipeline,
                    List.of("INCR", "a"));

            encodeCommand(
                    encoder,
                    pipeline,
                    List.of("GET", "a"));

            encodeCommand(
                    encoder,
                    pipeline,
                    List.of("GET", "b"));

            /*
             * Sending the entire pipeline in one write.
             */
            socket.getOutputStream()
                    .write(pipeline.toByteArray());

            socket.getOutputStream().flush();

            InputStream input = socket.getInputStream();

            /*
             * Responses must arrive in exactly
             * the same order as the commands.
             */

            assertEquals(
                    new RespValue.SimpleString("OK"),
                    decoder.decode(input));

            assertEquals(
                    new RespValue.SimpleString("OK"),
                    decoder.decode(input));

            assertEquals(
                    new RespValue.IntegerValue(2),
                    decoder.decode(input));

            assertEquals(
                    new RespValue.BulkString("2"),
                    decoder.decode(input));

            assertEquals(
                    new RespValue.BulkString("2"),
                    decoder.decode(input));
        }
    }

    private void encodeCommand(
            RespEncoder encoder,
            ByteArrayOutputStream output,
            List<String> arguments)
            throws IOException {

        List<RespValue> values =
                arguments.stream()
                        .map(RespValue.BulkString::new)
                        .map(value -> (RespValue) value)
                        .toList();

        encoder.encode(
                new RespValue.Array(values),
                output);
    }
}