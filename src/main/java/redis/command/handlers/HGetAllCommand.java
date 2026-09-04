package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisHash;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HGetAllCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() != 1) {
            return new RespValue.Error("ERR wrong number of arguments for 'hgetall'");
        }

        RedisHash hash = store.getHash(arguments.get(0));

        if (hash == null) return new RespValue.Array(List.of());

        Map<String, String> values = hash.all();

        List<RespValue> response = new ArrayList<>();

        for (Map.Entry<String, String> entry : values.entrySet()) {
            response.add(new RespValue.BulkString(entry.getKey()));
            response.add(new RespValue.BulkString(entry.getValue()));
        }

        return new RespValue.Array(response);
    }
}