package redis.command.handlers;

import redis.command.Command;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;

public class PingCommand implements Command {

    @Override
    public RespValue execute(DataStore store, CommandRequest request) {

        if (!request.arguments().isEmpty()) {
            return new RespValue.Error("ERR wrong number of arguments for 'ping'");
        }

        return new RespValue.SimpleString("PONG");
    }
}