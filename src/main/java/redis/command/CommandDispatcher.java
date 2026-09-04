package redis.command;

import redis.command.handlers.DecrCommand;
import redis.command.handlers.DelCommand;
import redis.command.handlers.EchoCommand;
import redis.command.handlers.ExistsCommand;
import redis.command.handlers.ExpireCommand;
import redis.command.handlers.GetCommand;
import redis.command.handlers.HDelCommand;
import redis.command.handlers.HExistsCommand;
import redis.command.handlers.HGetAllCommand;
import redis.command.handlers.HGetCommand;
import redis.command.handlers.HSetCommand;
import redis.command.handlers.IncrCommand;
import redis.command.handlers.LPopCommand;
import redis.command.handlers.LPushCommand;
import redis.command.handlers.LrangeCommand;
import redis.command.handlers.LlenCommand;
import redis.command.handlers.PexpireCommand;
import redis.command.handlers.PersistCommand;
import redis.command.handlers.PingCommand;
import redis.command.handlers.PttlCommand;
import redis.command.handlers.RPopCommand;
import redis.command.handlers.RPushCommand;
import redis.command.handlers.SAddCommand;
import redis.command.handlers.SCardCommand;
import redis.command.handlers.SIsMemberCommand;
import redis.command.handlers.SMembersCommand;
import redis.command.handlers.SRemCommand;
import redis.command.handlers.SetCommand;
import redis.command.handlers.TtlCommand;

import redis.protocol.RespValue;
import redis.storage.DataStore;

import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher {

    private final Map<String, Command> commands =
            new HashMap<>();

    public CommandDispatcher() {

        commands.put("PING", new PingCommand());
        commands.put("ECHO", new EchoCommand());

        commands.put("SET", new SetCommand());
        commands.put("GET", new GetCommand());

        commands.put("DEL", new DelCommand());
        commands.put("EXISTS", new ExistsCommand());

        commands.put("INCR", new IncrCommand());
        commands.put("DECR", new DecrCommand());

        commands.put("EXPIRE", new ExpireCommand());
        commands.put("PEXPIRE", new PexpireCommand());

        commands.put("TTL", new TtlCommand());
        commands.put("PTTL", new PttlCommand());

        commands.put("PERSIST", new PersistCommand());

        // Lists
        commands.put("LPUSH", new LPushCommand());
        commands.put("RPUSH", new RPushCommand());
        commands.put("LPOP", new LPopCommand());
        commands.put("RPOP", new RPopCommand());
        commands.put("LLEN", new LlenCommand());
        commands.put("LRANGE", new LrangeCommand());

        // Hashes
        commands.put("HSET", new HSetCommand());
        commands.put("HGET", new HGetCommand());
        commands.put("HGETALL", new HGetAllCommand());
        commands.put("HDEL", new HDelCommand());
        commands.put("HEXISTS", new HExistsCommand());

        // Sets
        commands.put("SADD", new SAddCommand());
        commands.put("SREM", new SRemCommand());
        commands.put("SISMEMBER", new SIsMemberCommand());
        commands.put("SMEMBERS", new SMembersCommand());
        commands.put("SCARD", new SCardCommand());
    }

    public RespValue dispatch(
            DataStore store,
            CommandRequest request) {

        Command command =
                commands.get(request.command());

        if (command == null) {

            return new RespValue.Error(
                    "ERR unknown command '"
                            + request.command()
                            + "'");
        }

        try {

            return command.execute(
                    store,
                    request);

        } catch (IllegalArgumentException e) {

            return new RespValue.Error(
                    e.getMessage());
        }
    }
}