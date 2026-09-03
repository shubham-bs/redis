package redis.command.handlers;

import org.junit.jupiter.api.Test;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpireCommandTest {

    @Test
    void setsExpirationOnExistingKey() {
        DataStore store = new DataStore();
        store.set(
                "name",
                new RedisString("shubham")
        );

        ExpireCommand command =
                new ExpireCommand();

        RespValue response =
                command.execute(
                        store,
                        new CommandRequest(
                                "EXPIRE",
                                List.of("name", "10")
                        )
                );

        assertEquals(
                new RespValue.IntegerValue(1),
                response
        );
    }

    @Test
    void returnsZeroForMissingKey() {
        ExpireCommand command =
                new ExpireCommand();

        RespValue response =
                command.execute(
                        new DataStore(),
                        new CommandRequest(
                                "EXPIRE",
                                List.of("unknown", "10")
                        )
                );

        assertEquals(
                new RespValue.IntegerValue(0),
                response
        );
    }

    @Test
    void rejectsInvalidTime() {
        ExpireCommand command =
                new ExpireCommand();

        RespValue response =
                command.execute(
                        new DataStore(),
                        new CommandRequest(
                                "EXPIRE",
                                List.of("name", "abc")
                        )
                );

        assertTrue(
                response instanceof RespValue.Error
        );
    }
}