package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisHash;

import java.util.List;

public class HSetCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() < 3 || arguments.size() % 2 == 0) {
            return new RespValue.Error("ERR wrong number of arguments for 'hset'");
        }

        String key = arguments.get(0);

        RedisHash hash = store.getOrCreateHash(key);

        long added = 0;

        for (int i = 1; i < arguments.size(); i += 2) {
            String field = arguments.get(i);
            String value = arguments.get(i + 1);
            if (hash.set(field, value)) added++;
        }

        return new RespValue.IntegerValue(added);
    }
}