package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;

import java.util.List;

public class DelCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() != 1)  return new RespValue.Error("ERR wrong number of arguments for 'del'");

        String key = arguments.get(0);

        boolean deleted = store.delete(key);

        return new RespValue.IntegerValue(deleted ? 1 : 0);
    }
}