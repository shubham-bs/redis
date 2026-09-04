package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisSet;

import java.util.ArrayList;
import java.util.List;

public class SMembersCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() != 1) {
            return new RespValue.Error("ERR wrong number of arguments for 'smembers'");
        }

        RedisSet set = store.getSet(arguments.get(0));

        if (set == null) return new RespValue.Array(List.of());

        List<RespValue> response = new ArrayList<>();

        for (String member : set.members()) {
            response.add(new RespValue.BulkString(member));
        }

        return new RespValue.Array(response);
    }
}