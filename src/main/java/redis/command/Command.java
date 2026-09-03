package redis.command;

import redis.protocol.RespValue;
import redis.storage.DataStore;

public interface Command {

    RespValue execute(DataStore store, CommandRequest request);
}