package redis.command;

import redis.protocol.RespValue;

import java.util.ArrayList;
import java.util.List;

public class CommandRequestParser {

    public CommandRequest parse(RespValue value) {

        if (!(value instanceof RespValue.Array array)) throw new IllegalArgumentException("Command must be a RESP array");
        if (array.values().isEmpty()) throw new IllegalArgumentException("Command cannot be empty");

        RespValue first = array.values().get(0);

        if (!(first instanceof RespValue.BulkString commandValue)) throw new IllegalArgumentException("Command name must be a bulk string");

        String command = commandValue.value().toUpperCase();

        List<String> arguments = new ArrayList<>();

        for (int i = 1; i < array.values().size(); i++) {
            RespValue argument = array.values().get(i);

            if (!(argument instanceof RespValue.BulkString bulkString)) throw new IllegalArgumentException("Command arguments must be bulk strings");

            arguments.add(bulkString.value());
        }

        return new CommandRequest(command, arguments);
    }
}