package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;

import java.util.List;

public class PexpireCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() != 2) {
            return new RespValue.Error("ERR wrong number of arguments for 'pexpire'");
        }

        String key = arguments.get(0);

        long milliseconds;

        try {
            milliseconds = Long.parseLong(arguments.get(1));
        } catch (NumberFormatException e) {
            return new RespValue.Error("ERR invalid expire time");
        }

        if (milliseconds < 0) {
            return new RespValue.Error("ERR invalid expire time");
        }

        boolean success = store.expireMillis(key, milliseconds);

        return new RespValue.IntegerValue(success ? 1 : 0);
    }
}