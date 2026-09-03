package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisString;

import java.util.List;

public class SetCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        List<String> arguments = request.arguments();

        if (arguments.size() < 2) {
            return new RespValue.Error("ERR wrong number of arguments for 'set'");
        }

        String key = arguments.get(0);
        String value = arguments.get(1);

        // Normal:
        // SET key value
        if (arguments.size() == 2) {
            store.set(key, new RedisString(value));

            return new RespValue.SimpleString("OK");
        }

        // SET key value EX seconds
        // SET key value PX milliseconds

        if (arguments.size() != 4) {
            return new RespValue.Error("ERR syntax error");
        }

        String option = arguments.get(2).toUpperCase();
        String expirationArgument = arguments.get(3);

        long milliseconds;

        try {
            long expiration = Long.parseLong(expirationArgument);
            if (expiration <= 0) {
                return new RespValue.Error("ERR invalid expire time");
            }

            if (option.equals("EX")) {
                milliseconds = Math.multiplyExact(expiration, 1000L);
            } else if (option.equals("PX")) {
                milliseconds = expiration;
            } else {
                return new RespValue.Error("ERR syntax error");
            }

        } catch (NumberFormatException e) {
            return new RespValue.Error("ERR invalid expire time");

        } catch (ArithmeticException e) {
            return new RespValue.Error("ERR invalid expire time");
        }

        store.setWithExpiration(key, new RedisString(value), milliseconds);

        return new RespValue.SimpleString("OK");
    }
}