package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisList;

import java.util.ArrayList;
import java.util.List;

public class LrangeCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() != 3) {
            return new RespValue.Error("ERR wrong number of arguments for 'lrange'");
        }

        long start;
        long stop;

        try {
            start = Long.parseLong(arguments.get(1));
            stop = Long.parseLong(arguments.get(2));
        } catch (NumberFormatException e) {
            return new RespValue.Error("ERR value is not an integer or out of range");
        }

        RedisList list = store.getList(arguments.get(0));

        if (list == null) return new RespValue.Array(List.of());

        List<String> values = list.range(start, stop);

        List<RespValue> response = new ArrayList<>();

        for (String value : values) {
            response.add(new RespValue.BulkString(value));
        }

        return new RespValue.Array(response);
    }
}