package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;

import java.util.List;

public class ExpireCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() != 2) {
            return new RespValue.Error("ERR wrong number of arguments for 'expire'");
        }

        String key = arguments.get(0);

        long seconds;

        try {
            seconds = Long.parseLong(arguments.get(1));

        } catch (NumberFormatException e) {
            return new RespValue.Error("ERR invalid expire time");
        }

        if (seconds < 0) return new RespValue.Error("ERR invalid expire time");

        boolean success = store.expire(key, seconds);

        return new RespValue.IntegerValue(success ? 1 : 0);
    }
}