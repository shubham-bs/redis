package redis.command;

import redis.command.handlers.*;
import redis.protocol.RespValue;
import redis.storage.DataStore;

import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher {

    private final Map<String, Command> commands;

    public CommandDispatcher() {

        commands = new HashMap<>();

        commands.put("SET", new SetCommand());
        commands.put("GET", new GetCommand());
        commands.put("PING", new PingCommand());
        commands.put("ECHO", new EchoCommand());
        commands.put("DEL", new DelCommand());
        commands.put("EXISTS", new ExistsCommand());
        commands.put("INCR", new IncrCommand());
        commands.put("DECR", new DecrCommand());
        commands.put("EXPIRE", new ExpireCommand());
        commands.put("TTL", new TtlCommand());
        commands.put("PEXPIRE", new PexpireCommand());
        commands.put("PTTL", new PttlCommand());
        commands.put("PERSIST", new PersistCommand());
    }

    public RespValue dispatch(DataStore store, CommandRequest request) {
        Command command = commands.get(request.command());

        if (command == null) return new RespValue.Error("ERR unknown command '" + request.command() + "'");

        return command.execute(store, request);
    }
}