package redis.command.handlers;

import org.junit.jupiter.api.Test;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PexpireCommandTest {

    private final PexpireCommand command = new PexpireCommand();

    @Test
    void shouldSetExpirationInMilliseconds() {
        DataStore store = new DataStore();
        store.set("name", new RedisString("shubham"));

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "PEXPIRE",
                        List.of("name", "1000")));

        assertEquals(
                new RespValue.IntegerValue(1),
                response);
    }

    @Test
    void shouldReturnZeroForMissingKey() {
        DataStore store = new DataStore();

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "PEXPIRE",
                        List.of("missing", "1000")));

        assertEquals(
                new RespValue.IntegerValue(0),
                response);
    }

    @Test
    void shouldRejectInvalidTime() {
        DataStore store = new DataStore();

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "PEXPIRE",
                        List.of("name", "-1")));

        assertTrue(response instanceof RespValue.Error);
    }
}