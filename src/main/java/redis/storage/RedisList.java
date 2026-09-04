package redis.storage;

import java.util.ArrayList;
import java.util.List;

public class RedisList implements RedisValue {

    private final List<String> values = new ArrayList<>();

    public synchronized void pushLeft(String value) {
        values.add(0, value);
    }

    public synchronized void pushRight(String value) {
        values.add(value);
    }

    public synchronized String popLeft() {
        if (values.isEmpty()) return null;
        return values.remove(0);
    }

    public synchronized String popRight() {
        if (values.isEmpty()) return null;
        return values.remove(values.size() - 1);
    }

    public synchronized int size() {
        return values.size();
    }

    public synchronized List<String> range(long start, long stop) {

        if (values.isEmpty()) return List.of();

        int size = values.size();

        int normalizedStart = normalizeIndex(start, size);
        int normalizedStop = normalizeIndex(stop, size);

        if (normalizedStart > normalizedStop) return List.of();

        return new ArrayList<>(values.subList(normalizedStart, normalizedStop + 1));
    }

    private int normalizeIndex(long index, int size) {

        if (index < 0) {
            long normalized = size + index;
            if (normalized < 0) return 0;
            return (int) normalized;
        }

        if (index >= size) return size - 1;

        return (int) index;
    }

    @Override
    public synchronized String toString() {
        return values.toString();
    }
}