package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisSet;

import java.util.List;

public class SAddCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() < 2) {
            return new RespValue.Error("ERR wrong number of arguments for 'sadd'");
        }

        RedisSet set = store.getOrCreateSet(arguments.get(0));

        long added = 0;

        for (int i = 1; i < arguments.size(); i++) {
            if (set.add(arguments.get(i))) added++;
        }

        return new RespValue.IntegerValue(added);
    }
}