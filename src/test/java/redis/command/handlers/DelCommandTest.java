package redis.command.handlers;

import org.junit.jupiter.api.Test;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DelCommandTest {

    @Test
    void deletesExistingKey() {
        DataStore store = new DataStore();
        store.set("name", new RedisString("shubham"));
        DelCommand command = new DelCommand();
        RespValue response =
                command.execute(
                        store,
                        new CommandRequest(
                                "DEL",
                                List.of("name")
                        )
                );

        assertEquals(
                new RespValue.IntegerValue(1),
                response
        );

        assertNull(store.get("name"));
    }

    @Test
    void returnsZeroForMissingKey() {
        DelCommand command = new DelCommand();
        RespValue response =
                command.execute(
                        new DataStore(),
                        new CommandRequest(
                                "DEL",
                                List.of("unknown")
                        )
                );

        assertEquals(
                new RespValue.IntegerValue(0),
                response
        );
    }
}