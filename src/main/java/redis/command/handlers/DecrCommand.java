package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;

import java.util.List;

public class DecrCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() != 1) return new RespValue.Error("ERR wrong number of arguments for 'decr'");

        String key = arguments.get(0);

        try {
            long newValue = store.decrement(key);
            return new RespValue.IntegerValue(newValue);
        } catch (IllegalArgumentException e) {
            return new RespValue.Error(e.getMessage());
        } catch (ArithmeticException e) {
            return new RespValue.Error("ERR increment or decrement would overflow");
        }
    }
}