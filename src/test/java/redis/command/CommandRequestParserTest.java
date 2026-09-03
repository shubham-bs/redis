package redis.command;

import org.junit.jupiter.api.Test;
import redis.protocol.RespValue;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandRequestParserTest {

    private final CommandRequestParser parser = new CommandRequestParser();

    @Test
    void parsesSetCommand() {
        RespValue request = new RespValue.Array(
                List.of(
                        new RespValue.BulkString("SET"),
                        new RespValue.BulkString("name"),
                        new RespValue.BulkString("Shubham")
                )
        );

        CommandRequest result = parser.parse(request);

        assertEquals("SET", result.command());

        assertEquals(List.of("name", "Shubham"), result.arguments());
    }

    @Test
    void parsesCommandWithoutArguments() {
        RespValue request = new RespValue.Array(
                List.of(
                        new RespValue.BulkString("PING")
                )
        );

        CommandRequest result = parser.parse(request);

        assertEquals("PING", result.command());
        assertTrue(result.arguments().isEmpty());
    }

    @Test
    void commandNameIsCaseInsensitive() {
        RespValue request = new RespValue.Array(
                List.of(
                        new RespValue.BulkString("set"),
                        new RespValue.BulkString("name"),
                        new RespValue.BulkString("Shubham")
                )
        );

        CommandRequest result = parser.parse(request);

        assertEquals("SET", result.command());
    }

    @Test
    void rejectsNonArrayRequest() {
        RespValue request = new RespValue.SimpleString("PING");

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(request)
        );
    }

    @Test
    void rejectsEmptyCommand() {
        RespValue request = new RespValue.Array(List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(request)
        );
    }
}