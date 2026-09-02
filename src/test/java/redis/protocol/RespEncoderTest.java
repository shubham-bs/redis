package redis.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RespEncoderTest {

    private final RespEncoder encoder = new RespEncoder();

    @Test
    void shouldEncodeSimpleString() throws Exception {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        encoder.encode(
                new RespValue.SimpleString("OK"),
                output
        );

        assertEquals(
                "+OK\r\n",
                output.toString(StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldEncodeError() throws Exception {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        encoder.encode(
                new RespValue.Error("ERR something went wrong"),
                output
        );

        assertEquals(
                "-ERR something went wrong\r\n",
                output.toString(StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldEncodeInteger() throws Exception {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        encoder.encode(
                new RespValue.IntegerValue(123),
                output
        );

        assertEquals(
                ":123\r\n",
                output.toString(StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldEncodeBulkString() throws Exception {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        encoder.encode(
                new RespValue.BulkString("hello"),
                output
        );

        assertEquals(
                "$5\r\nhello\r\n",
                output.toString(StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldEncodeNull() throws Exception {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        encoder.encode(
                new RespValue.NullValue(),
                output
        );

        assertEquals(
                "$-1\r\n",
                output.toString(StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldEncodeArray() throws Exception {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        encoder.encode(
                new RespValue.Array(
                        List.of(
                                new RespValue.BulkString("PING"),
                                new RespValue.BulkString("hello")
                        )
                ),
                output
        );

        assertEquals(
                "*2\r\n" +
                        "$4\r\n" +
                        "PING\r\n" +
                        "$5\r\n" +
                        "hello\r\n",
                output.toString(StandardCharsets.UTF_8)
        );
    }
}