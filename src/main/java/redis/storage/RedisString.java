package redis.storage;

public class RedisString implements RedisValue {

    private String value;

    public RedisString(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public void value(String value) {
        this.value = value;
    }
}