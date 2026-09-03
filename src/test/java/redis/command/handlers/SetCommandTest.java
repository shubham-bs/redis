package redis.command.handlers;

import org.junit.jupiter.api.Test;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SetCommandTest {

    private final SetCommand command = new SetCommand();

    @Test
    void shouldSetValue() {
        DataStore store = new DataStore();

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "SET",
                        List.of("name", "shubham")));

        assertEquals(
                new RespValue.SimpleString("OK"),
                response);

        assertEquals(
                "shubham",
                ((RedisString) store.get("name").value()).value());
    }

    @Test
    void shouldRejectMissingArguments() {
        DataStore store = new DataStore();

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "SET",
                        List.of("name")));

        assertTrue(
                response instanceof RespValue.Error);
    }

    @Test
    void shouldSetExpirationUsingEx() {
        DataStore store = new DataStore();

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "SET",
                        List.of("name", "shubham", "EX", "10")));

        assertEquals(
                new RespValue.SimpleString("OK"),
                response);

        long ttl = store.ttl("name");

        assertTrue(ttl >= 0);
        assertTrue(ttl <= 10);
    }

    @Test
    void shouldSetExpirationUsingPx() {
        DataStore store = new DataStore();

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "SET",
                        List.of("name", "shubham", "PX", "5000")));

        assertEquals(
                new RespValue.SimpleString("OK"),
                response);

        long pttl = store.pttl("name");

        assertTrue(pttl > 0);
        assertTrue(pttl <= 5000);
    }

    @Test
    void shouldRejectInvalidExpirationOption() {
        DataStore store = new DataStore();

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "SET",
                        List.of("name", "shubham", "XX", "10")));

        assertTrue(
                response instanceof RespValue.Error);
    }

    @Test
    void shouldRemoveOldExpirationWhenOverwritten() {
        DataStore store = new DataStore();

        command.execute(
                store,
                new CommandRequest(
                        "SET",
                        List.of("name", "shubham", "EX", "10")));

        command.execute(
                store,
                new CommandRequest(
                        "SET",
                        List.of("name", "new-value")));

        assertEquals(
                -1,
                store.ttl("name"));

        assertEquals(
                "new-value",
                ((RedisString) store.get("name").value()).value());
    }
}