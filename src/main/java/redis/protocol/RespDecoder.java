package redis.protocol;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RespDecoder {

    private static final int MAX_LINE_LENGTH = 64 * 1024;
    private static final int MAX_BULK_STRING_LENGTH = 10 * 1024 * 1024;
    private static final int MAX_ARRAY_LENGTH = 1_000_000;

    public RespValue decode(InputStream input) throws IOException {
        int type = input.read();

        if (type == -1) throw new EOFException("Connection closed");

        return switch (type) {
            case '+' -> new RespValue.SimpleString(readLine(input));
            case '-' -> new RespValue.Error(readLine(input));
            case ':' -> decodeInteger(input);
            case '$' -> decodeBulkString(input);
            case '*' -> decodeArray(input);
            default -> throw new IOException("Unknown RESP type: " + (char) type);
        };
    }

    private RespValue decodeInteger(InputStream input) throws IOException {
        String line = readLine(input);

        try {
            return new RespValue.IntegerValue(Long.parseLong(line));
        } catch (NumberFormatException e) {
            throw new IOException("Invalid integer", e);
        }
    }

    private RespValue decodeBulkString(InputStream input) throws IOException {
        String lengthLine = readLine(input);
        int length;

        try {
            length = Integer.parseInt(lengthLine);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid bulk string length", e);
        }

        if (length == -1) return new RespValue.NullValue();
        if (length < -1) throw new IOException("Invalid bulk string length");
        if (length > MAX_BULK_STRING_LENGTH) throw new IOException("Bulk string too large");

        byte[] data = readExactly(input, length);

        expectCRLF(input);

        return new RespValue.BulkString(new String(data, StandardCharsets.UTF_8));
    }

    private RespValue decodeArray(InputStream input) throws IOException {
        String countLine = readLine(input);
        int count;

        try {
            count = Integer.parseInt(countLine);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid array length", e);
        }

        if (count == -1) return new RespValue.NullValue();
        if (count < -1) throw new IOException("Invalid array length");
        if (count > MAX_ARRAY_LENGTH) throw new IOException("Array too large");

        List<RespValue> values = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            values.add(decode(input));
        }

        return new RespValue.Array(values);
    }

    private String readLine(InputStream input) throws IOException {
        StringBuilder result = new StringBuilder();

        while (true) {
            int current = input.read();

            if (current == -1) throw new EOFException("Unexpected end of input");

            if (current == '\r') {
                int next = input.read();

                if (next != '\n') throw new IOException("Expected LF after CR");

                return result.toString();
            }

            if (current == '\n') throw new IOException("Unexpected LF");

            result.append((char) current);

            if (result.length() > MAX_LINE_LENGTH) throw new IOException("RESP line too long");
        }
    }

    private byte[] readExactly(InputStream input, int length) throws IOException {

        byte[] data = new byte[length];

        int offset = 0;

        while (offset < length) {
            int bytesRead = input.read(data, offset, length - offset);

            if (bytesRead == -1) throw new EOFException("Unexpected end of bulk string");

            if (bytesRead == 0) continue;;

            offset += bytesRead;
        }

        return data;
    }

    private void expectCRLF(InputStream input) throws IOException {

        int first = input.read();
        int second = input.read();

        if (first != '\r' || second != '\n') throw new IOException("Expected CRLF");
    }
}