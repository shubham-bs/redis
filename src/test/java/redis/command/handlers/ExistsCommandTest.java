package redis.command.handlers;

import org.junit.jupiter.api.Test;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExistsCommandTest {

    @Test
    void returnsOneForExistingKey() {
        DataStore store = new DataStore();
        store.set(
                "name",
                new RedisString("shubham")
        );

        ExistsCommand command = new ExistsCommand();

        RespValue response =
                command.execute(
                        store,
                        new CommandRequest(
                                "EXISTS",
                                List.of("name")
                        )
                );

        assertEquals(
                new RespValue.IntegerValue(1),
                response
        );
    }

    @Test
    void returnsZeroForMissingKey() {
        ExistsCommand command = new ExistsCommand();

        RespValue response =
                command.execute(
                        new DataStore(),
                        new CommandRequest(
                                "EXISTS",
                                List.of("unknown")
                        )
                );

        assertEquals(
                new RespValue.IntegerValue(0),
                response
        );
    }
}