package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisHash;

import java.util.List;

public class HGetCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() != 2) {
            return new RespValue.Error("ERR wrong number of arguments for 'hget'");
        }

        RedisHash hash = store.getHash(arguments.get(0));

        if (hash == null) return new RespValue.NullValue();

        String value = hash.get(arguments.get(1));

        if (value == null) return new RespValue.NullValue();

        return new RespValue.BulkString(value);
    }
}