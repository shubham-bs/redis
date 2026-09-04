package redis.integration;

import org.junit.jupiter.api.Test;
import redis.network.RedisServer;
import redis.protocol.RespDecoder;
import redis.protocol.RespEncoder;
import redis.protocol.RespValue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProtocolIntegrationTest {

    @Test
    void shouldHandleMultipleCommandsInOneConnection() throws Exception {

        RedisServer server = new RedisServer(0);

        Thread serverThread = Thread.ofVirtual().start(() -> {
                    try {
                        server.start();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        server.awaitStarted();

        try (Socket socket = new Socket("localhost", server.getPort())) {

            InputStream input = socket.getInputStream();

            OutputStream output = socket.getOutputStream();

            RespEncoder encoder = new RespEncoder();

            RespDecoder decoder = new RespDecoder();

            ByteArrayOutputStream pipeline = new ByteArrayOutputStream();

            encoder.encode(command("SET", "day5", "hello"), pipeline);

            encoder.encode(command("GET", "day5"), pipeline);

            encoder.encode(command("INCR", "counter"), pipeline);

            encoder.encode(command("GET", "counter"), pipeline);

            output.write(pipeline.toByteArray());

            output.flush();

            assertEquals(new RespValue.SimpleString("OK"), decoder.decode(input));

            assertEquals(new RespValue.BulkString("hello"), decoder.decode(input));

            assertEquals(new RespValue.IntegerValue(1), decoder.decode(input));

            assertEquals(new RespValue.BulkString("1"), decoder.decode(input));

        } finally {
            server.stop();
            serverThread.join();
        }
    }

    private RespValue.Array command(String command, String... arguments) {

        List<RespValue> values = new ArrayList<>();

        values.add(new RespValue.BulkString(command));

        for (String argument : arguments) {
            values.add(new RespValue.BulkString(argument));
        }

        return new RespValue.Array(values);
    }
}