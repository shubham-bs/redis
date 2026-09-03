package redis.command.handlers;

import org.junit.jupiter.api.Test;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PingCommandTest {

    @Test
    void pingReturnsPong() {

        PingCommand command = new PingCommand();

        RespValue response =
                command.execute(
                        new DataStore(),
                        new CommandRequest("PING", List.of())
                );

        assertEquals(
                new RespValue.SimpleString("PONG"),
                response
        );
    }

    @Test
    void pingRejectsArguments() {

        PingCommand command = new PingCommand();

        RespValue response =
                command.execute(
                        new DataStore(),
                        new CommandRequest(
                                "PING",
                                List.of("hello")
                        )
                );

        assertTrue(response instanceof RespValue.Error);
    }
}