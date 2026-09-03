package redis.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RespDecoderEdgeCaseTest {

    private final RespDecoder decoder = new RespDecoder();

    @Test
    void shouldDecodeMultipleRequestsFromSameInput() throws Exception {

        String input = "*1\r\n" +
                        "$4\r\n" +
                        "PING\r\n" +
                        "*2\r\n" +
                        "$4\r\n" +
                        "ECHO\r\n" +
                        "$5\r\n" +
                        "hello\r\n";

        ByteArrayInputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

        RespValue first = decoder.decode(stream);
        RespValue second = decoder.decode(stream);

        assertEquals(new RespValue.Array(
                java.util.List.of(new RespValue.BulkString("PING"))), first);

        assertEquals(new RespValue.Array(
                        java.util.List.of(
                                new RespValue.BulkString("ECHO"),
                                new RespValue.BulkString("hello")
                        )), second);
    }

    @Test
    void shouldDecodeFragmentedRequest() throws Exception {

        byte[][] chunks = {
                "*1\r\n$4\r\nPI".getBytes(StandardCharsets.UTF_8),
                "NG\r\n".getBytes(StandardCharsets.UTF_8)
        };

        ChunkedInputStream stream = new ChunkedInputStream(chunks);

        RespValue result = decoder.decode(stream);

        assertEquals(new RespValue.Array(
                        java.util.List.of(
                                new RespValue.BulkString("PING")
                        )), result);
    }

    private static class ChunkedInputStream extends java.io.InputStream {

        private final byte[][] chunks;
        private int chunkIndex;
        private int position;

        ChunkedInputStream(byte[][] chunks) {
            this.chunks = chunks;
        }

        @Override
        public int read() {

            while (chunkIndex < chunks.length) {

                byte[] currentChunk = chunks[chunkIndex];

                if (position < currentChunk.length) return currentChunk[position++] & 0xFF;

                chunkIndex++;
                position = 0;
            }

            return -1;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) {

            if (chunkIndex >= chunks.length) return -1;

            byte[] currentChunk = chunks[chunkIndex];

            if (position >= currentChunk.length) {
                chunkIndex++;
                position = 0;
                return read(buffer, offset, length);
            }

            int bytesToCopy = Math.min(length, currentChunk.length - position);

            System.arraycopy(
                    currentChunk,
                    position,
                    buffer,
                    offset,
                    bytesToCopy);

            position += bytesToCopy;

            return bytesToCopy;
        }
    }
}