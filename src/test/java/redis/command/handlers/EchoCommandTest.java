package redis.command.handlers;

import org.junit.jupiter.api.Test;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EchoCommandTest {

    @Test
    void echoReturnsArgument() {
        EchoCommand command = new EchoCommand();
        RespValue response =
                command.execute(
                        new DataStore(),
                        new CommandRequest(
                                "ECHO",
                                List.of("hello")
                        )
                );

        assertEquals(
                new RespValue.BulkString("hello"),
                response
        );
    }

    @Test
    void echoRequiresExactlyOneArgument() {
        EchoCommand command = new EchoCommand();
        RespValue response =
                command.execute(
                        new DataStore(),
                        new CommandRequest(
                                "ECHO",
                                List.of()
                        )
                );

        assertTrue(response instanceof RespValue.Error);
    }
}