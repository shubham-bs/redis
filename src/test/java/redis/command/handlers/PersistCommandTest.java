package redis.command.handlers;

import org.junit.jupiter.api.Test;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersistCommandTest {

    private final PersistCommand command = new PersistCommand();

    @Test
    void shouldRemoveExpiration() {
        DataStore store = new DataStore();

        store.set("name", new RedisString("shubham"));
        store.expire("name", 10);

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "PERSIST",
                        List.of("name")));

        assertEquals(
                new RespValue.IntegerValue(1),
                response);

        assertEquals(-1, store.ttl("name"));

        assertEquals(
                "shubham",
                ((RedisString) store.get("name").value()).value());
    }

    @Test
    void shouldReturnZeroForMissingKey() {
        DataStore store = new DataStore();

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "PERSIST",
                        List.of("missing")));

        assertEquals(
                new RespValue.IntegerValue(0),
                response);
    }

    @Test
    void shouldReturnZeroForPersistentKey() {
        DataStore store = new DataStore();

        store.set("name", new RedisString("shubham"));

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "PERSIST",
                        List.of("name")));

        assertEquals(
                new RespValue.IntegerValue(0),
                response);
    }

    @Test
    void shouldRejectWrongNumberOfArguments() {
        DataStore store = new DataStore();

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "PERSIST",
                        List.of()));

        assertTrue(response instanceof RespValue.Error);
    }
}