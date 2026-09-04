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
import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DataTypesIntegrationTest {

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
    void shouldSupportListsHashesAndSets()
            throws Exception {

        try (
                Socket socket = new Socket("localhost", server.getPort())
        ) {
            RespEncoder encoder = new RespEncoder();
            RespDecoder decoder = new RespDecoder();

            // LPUSH
            send(encoder, socket, List.of("LPUSH", "fruits", "apple"));
            assertEquals(new RespValue.IntegerValue(1), decoder.decode(socket.getInputStream()));

            // RPUSH
            send(encoder, socket, List.of("RPUSH", "fruits", "banana"));
            assertEquals(new RespValue.IntegerValue(2), decoder.decode(socket.getInputStream()));

            // LLEN
            send(encoder, socket, List.of("LLEN", "fruits"));
            assertEquals(new RespValue.IntegerValue(2), decoder.decode(socket.getInputStream()));

            // LRANGE
            send(encoder, socket, List.of("LRANGE", "fruits", "0", "-1"));
            assertEquals(new RespValue.Array(
                            List.of(
                                    new RespValue.BulkString("apple"),
                                    new RespValue.BulkString("banana"))),
                    decoder.decode(socket.getInputStream()));

            // HSET
            send(encoder, socket, List.of("HSET", "user", "name", "Shubham"));
            assertEquals(new RespValue.IntegerValue(1), decoder.decode(socket.getInputStream()));

            // HGET
            send(encoder, socket, List.of("HGET", "user", "name"));
            assertEquals(new RespValue.BulkString("Shubham"), decoder.decode(socket.getInputStream()));

            // HEXISTS
            send(encoder, socket, List.of("HEXISTS", "user", "name"));
            assertEquals(new RespValue.IntegerValue(1), decoder.decode(socket.getInputStream()));

            // SADD
            send(encoder, socket, List.of("SADD", "skills", "java", "spring"));
            assertEquals(new RespValue.IntegerValue(2), decoder.decode(socket.getInputStream()));

            // duplicate
            send(encoder, socket, List.of("SADD", "skills", "java"));
            assertEquals(new RespValue.IntegerValue(0), decoder.decode(socket.getInputStream()));

            // SCARD
            send(encoder, socket, List.of("SCARD", "skills"));
            assertEquals(new RespValue.IntegerValue(2), decoder.decode(socket.getInputStream()));

            // SISMEMBER
            send(encoder, socket, List.of("SISMEMBER", "skills", "java"));
            assertEquals(new RespValue.IntegerValue(1), decoder.decode(socket.getInputStream()));

            // Type enforcement
            send(encoder, socket, List.of("GET", "fruits"));
            RespValue response = decoder.decode(socket.getInputStream());
            RespValue.Error error = assertInstanceOf(RespValue.Error.class, response);
            assertEquals("WRONGTYPE Operation against a key holding the wrong kind of value",
                    error.value());
            // List command against String
            send(encoder, socket, List.of("SET", "plain", "hello"));
            assertEquals(new RespValue.SimpleString("OK"), decoder.decode(socket.getInputStream()));
            send(encoder, socket, List.of("LPUSH", "plain", "value"));
            response = decoder.decode(socket.getInputStream());
            error = assertInstanceOf(RespValue.Error.class, response);
            assertEquals("WRONGTYPE", error.value());
        }
    }

    private void send(
            RespEncoder encoder,
            Socket socket,
            List<String> arguments)
            throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        List<RespValue> values = arguments.stream()
                        .map(RespValue.BulkString::new)
                        .map(value -> (RespValue) value)
                        .toList();

        encoder.encode(new RespValue.Array(values), output);

        socket.getOutputStream().write(output.toByteArray());

        socket.getOutputStream().flush();
    }
}