package redis.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RespDecoderHardeningTest {

    private final RespDecoder decoder = new RespDecoder();

    @Test
    void shouldRejectInvalidInteger() {
        String input = ":not-a-number\r\n";
        assertThrows(Exception.class, () -> decoder.decode(stream(input)));
    }

    @Test
    void shouldRejectInvalidBulkStringLength() {
        String input = "$-5\r\n";
        assertThrows(Exception.class, () -> decoder.decode(stream(input)));
    }

    @Test
    void shouldRejectInvalidArrayLength() {
        String input = "*-5\r\n";
        assertThrows(Exception.class, () -> decoder.decode(stream(input)));
    }

    @Test
    void shouldRejectMalformedCrlf() {
        String input = "+PING\n";
        assertThrows(Exception.class, () -> decoder.decode(stream(input)));
    }

    @Test
    void shouldRejectUnknownRespType() {
        String input = "!hello\r\n";
        assertThrows(Exception.class, () -> decoder.decode(stream(input)));
    }

    @Test
    void shouldDecodeNullBulkString() throws Exception {
        String input = "$-1\r\n";
        RespValue value = decoder.decode(stream(input));
        assertEquals(new RespValue.NullValue(), value);
    }

    @Test
    void shouldDecodeNullArray() throws Exception {
        String input = "*-1\r\n";
        RespValue value = decoder.decode(stream(input));
        assertEquals(new RespValue.NullValue(), value);
    }

    @Test
    void shouldRejectIncompleteBulkString() {
        String input = "$5\r\nhello";
        assertThrows(Exception.class, () -> decoder.decode(stream(input)));
    }

    @Test
    void shouldRejectIncompleteArray() {
        String input = "*2\r\n$3\r\nGET\r\n";
        assertThrows(Exception.class, () -> decoder.decode(stream(input)));
    }

    private ByteArrayInputStream stream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}