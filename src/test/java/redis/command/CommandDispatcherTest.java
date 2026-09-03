package redis.command;

import org.junit.jupiter.api.Test;
import redis.protocol.RespValue;
import redis.storage.DataStore;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandDispatcherTest {

    @Test
    void dispatchesSetCommand() {

        DataStore store = new DataStore();
        CommandDispatcher dispatcher = new CommandDispatcher();

        CommandRequest request = new CommandRequest("SET", List.of("name", "Shubham"));

        RespValue response = dispatcher.dispatch(store, request);

        assertInstanceOf(RespValue.SimpleString.class, response);

        assertEquals("OK", ((RespValue.SimpleString) response).value());
    }

    @Test
    void dispatchesGetCommand() {

        DataStore store = new DataStore();
        CommandDispatcher dispatcher = new CommandDispatcher();

        CommandRequest setRequest = new CommandRequest("SET", List.of("name", "Shubham"));

        dispatcher.dispatch(store, setRequest);

        CommandRequest getRequest = new CommandRequest("GET", List.of("name"));

        RespValue response = dispatcher.dispatch(store, getRequest);

        assertInstanceOf(RespValue.BulkString.class, response);

        assertEquals("Shubham", ((RespValue.BulkString) response).value());
    }

    @Test
    void returnsErrorForUnknownCommand() {

        DataStore store = new DataStore();
        CommandDispatcher dispatcher = new CommandDispatcher();

        CommandRequest request = new CommandRequest("BLAH", List.of());

        RespValue response = dispatcher.dispatch(store, request);

        assertInstanceOf(RespValue.Error.class, response);
    }
}