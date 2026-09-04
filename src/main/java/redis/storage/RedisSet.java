package redis.storage;

import java.util.LinkedHashSet;
import java.util.Set;

public class RedisSet implements RedisValue {

    private final Set<String> values = new LinkedHashSet<>();

    public synchronized boolean add(String member) {
        return values.add(member);
    }

    public synchronized boolean remove(String member) {
        return values.remove(member);
    }

    public synchronized boolean contains(String member) {
        return values.contains(member);
    }

    public synchronized Set<String> members() {
        return new LinkedHashSet<>(values);
    }

    public synchronized int size() {
        return values.size();
    }

    @Override
    public synchronized String toString() {
        return values.toString();
    }
}