package redis.protocol;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RespDecoder {

    public RespValue decode(InputStream input) throws IOException {

        int type = input.read();

        if (type == -1) {
            throw new EOFException("Connection closed");
        }

        return switch (type) {

            case '+' -> new RespValue.SimpleString(readLine(input));

            case '-' -> new RespValue.Error(readLine(input));

            case ':' -> new RespValue.IntegerValue(
                    Long.parseLong(readLine(input))
            );

            case '$' -> decodeBulkString(input);

            case '*' -> decodeArray(input);

            default -> throw new IOException(
                    "Unknown RESP type: " + (char) type
            );
        };
    }

    private RespValue decodeBulkString(InputStream input)
            throws IOException {

        int length = Integer.parseInt(readLine(input));

        if (length == -1) {
            return new RespValue.NullValue();
        }

        if (length < -1) {
            throw new IOException("Invalid bulk string length");
        }

        byte[] data = readExactly(input, length);

        expectCRLF(input);

        return new RespValue.BulkString(
                new String(data, StandardCharsets.UTF_8)
        );
    }

    private RespValue decodeArray(InputStream input)
            throws IOException {

        int count = Integer.parseInt(readLine(input));

        if (count == -1) {
            return new RespValue.NullValue();
        }

        if (count < -1) {
            throw new IOException("Invalid array length");
        }

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

            if (current == -1) {
                throw new EOFException("Unexpected end of input");
            }

            if (current == '\r') {

                int next = input.read();

                if (next != '\n') {
                    throw new IOException("Expected LF after CR");
                }

                return result.toString();
            }

            result.append((char) current);
        }
    }

    private byte[] readExactly(InputStream input, int length)
            throws IOException {

        byte[] data = new byte[length];

        int offset = 0;

        while (offset < length) {

            int bytesRead = input.read(
                    data,
                    offset,
                    length - offset
            );

            if (bytesRead == -1) {
                throw new EOFException(
                        "Unexpected end of bulk string"
                );
            }

            offset += bytesRead;
        }

        return data;
    }

    private void expectCRLF(InputStream input)
            throws IOException {

        int first = input.read();
        int second = input.read();

        if (first != '\r' || second != '\n') {
            throw new IOException("Expected CRLF");
        }
    }
}