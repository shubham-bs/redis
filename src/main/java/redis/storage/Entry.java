package redis.storage;

public class Entry {

    private final RedisValue value;

    public Entry(RedisValue value) {
        this.value = value;
    }

    public RedisValue value() {
        return value;
    }
}