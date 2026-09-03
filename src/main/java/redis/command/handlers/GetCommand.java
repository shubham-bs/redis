package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.Entry;
import redis.storage.RedisString;

import java.util.List;

public class GetCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() != 1) {
            return new RespValue.Error("ERR wrong number of arguments for 'get'");
        }

        String key = arguments.get(0);

        Entry entry = store.get(key);

        if (entry == null) return new RespValue.NullValue();

        if (!(entry.value() instanceof RedisString value)) {
            return new RespValue.Error("WRONGTYPE Operation against a key holding the wrong kind of value");
        }

        return new RespValue.BulkString(value.value());
    }
}