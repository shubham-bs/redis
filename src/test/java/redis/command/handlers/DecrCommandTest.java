package redis.command.handlers;

import org.junit.jupiter.api.Test;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DecrCommandTest {

    @Test
    void decrementsExistingInteger() {
        DataStore store = new DataStore();
        store.set("counter", new RedisString("10"));

        DecrCommand command = new DecrCommand();

        RespValue response =
                command.execute(
                        store,
                        new CommandRequest(
                                "DECR",
                                List.of("counter")));

        assertEquals(
                new RespValue.IntegerValue(9),
                response
        );
    }

    @Test
    void createsMissingKeyWithNegativeOne() {
        DataStore store = new DataStore();
        DecrCommand command = new DecrCommand();
        RespValue response =
                command.execute(
                        store,
                        new CommandRequest(
                                "DECR",
                                List.of("counter")
                        )
                );

        assertEquals(
                new RespValue.IntegerValue(-1),
                response
        );
    }

    @Test
    void rejectsNonInteger() {
        DataStore store = new DataStore();
        store.set(
                "counter",
                new RedisString("hello")
        );

        DecrCommand command = new DecrCommand();

        RespValue response =
                command.execute(
                        store,
                        new CommandRequest(
                                "DECR",
                                List.of("counter")
                        )
                );

        assertTrue(response instanceof RespValue.Error);
    }

    @Test
    void rejectsWrongNumberOfArguments() {
        DecrCommand command = new DecrCommand();

        RespValue response =
                command.execute(
                        new DataStore(),
                        new CommandRequest(
                                "DECR",
                                List.of()
                        )
                );

        assertTrue(response instanceof RespValue.Error);
    }
}