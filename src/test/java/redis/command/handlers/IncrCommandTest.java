package redis.command.handlers;

import org.junit.jupiter.api.Test;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IncrCommandTest {

    @Test
    void incrementsExistingInteger() {

        DataStore store = new DataStore();

        store.set(
                "counter",
                new RedisString("10")
        );

        IncrCommand command = new IncrCommand();

        RespValue response =
                command.execute(
                        store,
                        new CommandRequest(
                                "INCR",
                                List.of("counter")
                        )
                );

        assertEquals(
                new RespValue.IntegerValue(11),
                response
        );

        assertEquals(
                "11",
                ((RedisString) store.get("counter").value()).value()
        );
    }

    @Test
    void createsMissingKeyWithOne() {

        DataStore store = new DataStore();

        IncrCommand command = new IncrCommand();

        RespValue response =
                command.execute(
                        store,
                        new CommandRequest(
                                "INCR",
                                List.of("counter")
                        )
                );

        assertEquals(
                new RespValue.IntegerValue(1),
                response
        );

        assertEquals(
                "1",
                ((RedisString) store.get("counter").value()).value()
        );
    }

    @Test
    void incrementsNegativeInteger() {

        DataStore store = new DataStore();

        store.set(
                "counter",
                new RedisString("-5")
        );

        IncrCommand command = new IncrCommand();

        RespValue response =
                command.execute(
                        store,
                        new CommandRequest(
                                "INCR",
                                List.of("counter")
                        )
                );

        assertEquals(
                new RespValue.IntegerValue(-4),
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

        IncrCommand command = new IncrCommand();

        RespValue response =
                command.execute(
                        store,
                        new CommandRequest(
                                "INCR",
                                List.of("counter")
                        )
                );

        assertTrue(response instanceof RespValue.Error);
    }

    @Test
    void rejectsWrongNumberOfArguments() {

        IncrCommand command = new IncrCommand();

        RespValue response =
                command.execute(
                        new DataStore(),
                        new CommandRequest(
                                "INCR",
                                List.of()
                        )
                );

        assertTrue(response instanceof RespValue.Error);
    }
}