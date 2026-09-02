package redis.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RespDecoderTest {

    private final RespDecoder decoder = new RespDecoder();

    @Test
    void shouldDecodePing() throws Exception {

        String input =
                "*1\r\n" +
                        "$4\r\n" +
                        "PING\r\n";

        RespValue result = decoder.decode(
                new ByteArrayInputStream(
                        input.getBytes(StandardCharsets.UTF_8)
                )
        );

        RespValue.Array array = assertInstanceOf(
                RespValue.Array.class,
                result
        );

        assertEquals(1, array.values().size());

        RespValue.BulkString command =
                assertInstanceOf(
                        RespValue.BulkString.class,
                        array.values().get(0)
                );

        assertEquals("PING", command.value());
    }

    @Test
    void shouldDecodeSetCommand() throws Exception {

        String input =
                "*3\r\n" +
                        "$3\r\n" +
                        "SET\r\n" +
                        "$3\r\n" +
                        "foo\r\n" +
                        "$3\r\n" +
                        "bar\r\n";

        RespValue result = decoder.decode(
                new ByteArrayInputStream(
                        input.getBytes(StandardCharsets.UTF_8)
                )
        );

        assertEquals(
                new RespValue.Array(
                        List.of(
                                new RespValue.BulkString("SET"),
                                new RespValue.BulkString("foo"),
                                new RespValue.BulkString("bar")
                        )
                ),
                result
        );
    }

    @Test
    void shouldDecodeInteger() throws Exception {

        RespValue result = decoder.decode(
                new ByteArrayInputStream(
                        ":123\r\n".getBytes(StandardCharsets.UTF_8)
                )
        );

        assertEquals(
                new RespValue.IntegerValue(123),
                result
        );
    }

    @Test
    void shouldDecodeNullBulkString() throws Exception {

        RespValue result = decoder.decode(
                new ByteArrayInputStream(
                        "$-1\r\n".getBytes(StandardCharsets.UTF_8)
                )
        );

        assertInstanceOf(
                RespValue.NullValue.class,
                result
        );
    }
}