package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisList;

import java.util.List;

public class RPushCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() < 2) {
            return new RespValue.Error("ERR wrong number of arguments for 'rpush'");
        }

        String key = arguments.get(0);

        RedisList list = store.getOrCreateList(key);

        for (int i = 1; i < arguments.size(); i++) {
            list.pushRight(arguments.get(i));
        }

        return new RespValue.IntegerValue(
                list.size());
    }
}