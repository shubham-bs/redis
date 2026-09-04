package redis.storage;

import java.util.LinkedHashMap;
import java.util.Map;

public class RedisHash implements RedisValue {

    private final Map<String, String> values = new LinkedHashMap<>();

    public synchronized boolean set(String field, String value) {

        boolean isNew = !values.containsKey(field);

        values.put(field, value);

        return isNew;
    }

    public synchronized String get(String field) {
        return values.get(field);
    }

    public synchronized boolean delete(String field) {
        return values.remove(field) != null;
    }

    public synchronized boolean exists(String field) {
        return values.containsKey(field);
    }

    public synchronized Map<String, String> all() {
        return new LinkedHashMap<>(values);
    }

    @Override
    public synchronized String toString() {
        return values.toString();
    }
}