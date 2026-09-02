package redis.protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class RespEncoder {

    public void encode(RespValue value, OutputStream output) throws IOException {

        if (value instanceof RespValue.SimpleString simpleString) {
            writeSimpleString(simpleString.value(), output);

        } else if (value instanceof RespValue.Error error) {
            writeError(error.value(), output);

        } else if (value instanceof RespValue.IntegerValue integer) {
            writeInteger(integer.value(), output);

        } else if (value instanceof RespValue.BulkString bulkString) {
            writeBulkString(bulkString.value(), output);

        } else if (value instanceof RespValue.Array array) {
            writeArray(array, output);

        } else if (value instanceof RespValue.NullValue) {
            output.write("$-1\r\n".getBytes(StandardCharsets.UTF_8));

        } else {
            throw new IllegalArgumentException("Unknown RESP value");
        }
    }

    private void writeSimpleString(String value, OutputStream output)
            throws IOException {

        write("+" + value + "\r\n", output);
    }

    private void writeError(String value, OutputStream output)
            throws IOException {

        write("-" + value + "\r\n", output);
    }

    private void writeInteger(long value, OutputStream output)
            throws IOException {

        write(":" + value + "\r\n", output);
    }

    private void writeBulkString(String value, OutputStream output)
            throws IOException {

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        write("$" + bytes.length + "\r\n", output);
        output.write(bytes);
        write("\r\n", output);
    }

    private void writeArray(RespValue.Array array, OutputStream output)
            throws IOException {

        write("*" + array.values().size() + "\r\n", output);

        for (RespValue value : array.values()) {
            encode(value, output);
        }
    }

    private void write(String value, OutputStream output)
            throws IOException {

        output.write(value.getBytes(StandardCharsets.UTF_8));
    }
}