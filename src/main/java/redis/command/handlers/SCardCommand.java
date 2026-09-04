package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisSet;

import java.util.List;

public class SCardCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() != 1) {
            return new RespValue.Error("ERR wrong number of arguments for 'scard'");
        }

        RedisSet set = store.getSet(arguments.get(0));

        if (set == null) return new RespValue.IntegerValue(0);

        return new RespValue.IntegerValue(set.size());
    }
}