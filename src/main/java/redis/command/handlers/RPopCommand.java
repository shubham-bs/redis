package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisList;

import java.util.List;

public class RPopCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() != 1) {
            return new RespValue.Error("ERR wrong number of arguments for 'rpop'");
        }

        RedisList list = store.getList(arguments.get(0));

        if (list == null) return new RespValue.NullValue();

        String value = list.popRight();

        if (value == null) return new RespValue.NullValue();

        return new RespValue.BulkString(value);
    }
}