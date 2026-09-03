package redis.command.handlers;

import org.junit.jupiter.api.Test;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PttlCommandTest {

    private final PttlCommand command = new PttlCommand();

    @Test
    void shouldReturnMinusTwoForMissingKey() {
        DataStore store = new DataStore();

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "PTTL",
                        List.of("missing")));

        assertEquals(
                new RespValue.IntegerValue(-2),
                response);
    }

    @Test
    void shouldReturnMinusOneForPersistentKey() {
        DataStore store = new DataStore();
        store.set("name", new RedisString("shubham"));

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "PTTL",
                        List.of("name")));

        assertEquals(
                new RespValue.IntegerValue(-1),
                response);
    }

    @Test
    void shouldReturnMillisecondsForExpiringKey() {
        DataStore store = new DataStore();
        store.set("name", new RedisString("shubham"));
        store.expireMillis("name", 5000);

        RespValue response = command.execute(
                store,
                new CommandRequest(
                        "PTTL",
                        List.of("name")));

        long value =
                ((RespValue.IntegerValue) response).value();

        assertTrue(value > 0);
        assertTrue(value <= 5000);
    }
}