package redis;

import redis.network.RedisServer;

public class Main {

    public static void main(String[] args) throws Exception {
        RedisServer server = new RedisServer(6379);
        server.start();
    }
}