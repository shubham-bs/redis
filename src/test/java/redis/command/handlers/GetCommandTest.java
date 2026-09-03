package redis.command.handlers;

import org.junit.jupiter.api.Test;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GetCommandTest {

    @Test
    void getsExistingValue() {
        DataStore store = new DataStore();
        store.set(
                "name",
                new RedisString("Shubham")
        );

        CommandRequest request =
                new CommandRequest(
                        "GET",
                        List.of("name")
                );

        RespValue response =
                new GetCommand().execute(store, request);

        assertInstanceOf(
                RespValue.BulkString.class,
                response
        );

        RespValue.BulkString bulk =
                (RespValue.BulkString) response;

        assertEquals(
                "Shubham",
                bulk.value()
        );
    }

    @Test
    void returnsNullForMissingKey() {
        DataStore store = new DataStore();
        CommandRequest request =
                new CommandRequest(
                        "GET",
                        List.of("missing")
                );

        RespValue response =
                new GetCommand().execute(store, request);

        assertInstanceOf(
                RespValue.NullValue.class,
                response
        );
    }

    @Test
    void rejectsWrongNumberOfArguments() {
        DataStore store = new DataStore();

        CommandRequest request =
                new CommandRequest(
                        "GET",
                        List.of()
                );

        RespValue response =
                new GetCommand().execute(store, request);

        assertInstanceOf(
                RespValue.Error.class,
                response
        );
    }
}