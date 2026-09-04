package redis.storage;

import redis.expiration.ExpirationManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private final Map<String, Entry> data = new ConcurrentHashMap<>();

    private final ExpirationManager expirationManager = new ExpirationManager();

    public void set(String key, RedisValue value) {
        data.put(key, new Entry(value));
        expirationManager.removeExpiration(key);
    }

    public Entry get(String key) {
        removeIfExpired(key);
        return data.get(key);
    }

    public boolean delete(String key) {
        removeIfExpired(key);
        boolean deleted = data.remove(key) != null;

        if (deleted) expirationManager.removeExpiration(key);

        return deleted;
    }

    public boolean exists(String key) {
        removeIfExpired(key);
        return data.containsKey(key);
    }

    public boolean expire(String key, long seconds) {
        return expireMillis(key, seconds * 1000L);
    }

    public boolean expireMillis(String key, long milliseconds) {

        if (get(key) == null) return false;

        long expirationTime = System.currentTimeMillis() + milliseconds;

        expirationManager.setExpiration(key, expirationTime);

        return true;
    }

    public long ttl(String key) {
        long pttl = pttl(key);
        if (pttl < 0) return pttl;
        return pttl / 1000L;
    }

    public long pttl(String key) {
        if (get(key) == null) return -2;

        Long expirationTime = expirationManager.getExpiration(key);

        if (expirationTime == null) return -1;

        long remainingMillis = expirationTime - System.currentTimeMillis();

        if (remainingMillis <= 0) {
            removeIfExpired(key);
            return -2;
        }

        return remainingMillis;
    }

    public boolean persist(String key) {
        if (get(key) == null) return false;

        Long expiration = expirationManager.getExpiration(key);

        if (expiration == null) return false;

        expirationManager.removeExpiration(key);

        return true;
    }

    public void setWithExpiration(String key, RedisValue value, long milliseconds) {
        data.put(key, new Entry(value));

        long expirationTime = System.currentTimeMillis() + milliseconds;

        expirationManager.setExpiration(key, expirationTime);
    }

    private void removeIfExpired(String key) {
        if (!expirationManager.isExpired(key)) return;

        data.remove(key);

        expirationManager.removeExpiration(key);
    }

    public long increment(String key) {

        removeIfExpired(key);

        final long[] result = new long[1];

        data.compute(key, (k, entry) -> {

                    if (entry == null) {
                        result[0] = 1;
                        return new Entry(new RedisString("1"));
                    }

                    if (!(entry.value() instanceof RedisString value)) {
                        throw new IllegalArgumentException("WRONGTYPE");
                    }

                    long currentValue;

                    try {
                        currentValue = Long.parseLong(value.value());
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("ERR value is not an integer or out of range");
                    }

                    long newValue;

                    try {
                        newValue = Math.addExact(currentValue, 1);
                    } catch (ArithmeticException e) {
                        throw new IllegalArgumentException("ERR value is not an integer or out of range");
                    }

                    result[0] = newValue;

                    value.value(String.valueOf(newValue));

                    return entry;
                });

        return result[0];
    }

    public long decrement(String key) {

        removeIfExpired(key);

        final long[] result = new long[1];

        data.compute(key,
                (k, entry) -> {
                    if (entry == null) {
                        result[0] = -1;
                        return new Entry(new RedisString("-1"));
                    }
                    if (!(entry.value() instanceof RedisString value)) {
                        throw new IllegalArgumentException("WRONGTYPE");
                    }
                    long currentValue;
                    try {
                        currentValue = Long.parseLong(value.value());
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("ERR value is not an integer or out of range");
                    }
                    long newValue;
                    try {
                        newValue = Math.subtractExact(currentValue, 1);
                    } catch (ArithmeticException e) {
                        throw new IllegalArgumentException("ERR value is not an integer or out of range");
                    }

                    result[0] = newValue;

                    value.value(String.valueOf(newValue));

                    return entry;
                });

        return result[0];
    }

    // ---------------------------------------------------------
    // LISTS
    // ---------------------------------------------------------

    public RedisList getOrCreateList(String key) {
        removeIfExpired(key);
        return (RedisList) data.compute(key,
                                (k, entry) -> {
                                    if (entry == null) {
                                        return new Entry(new RedisList());
                                    }
                                    if (!(entry.value() instanceof RedisList)) {
                                        throw new IllegalArgumentException("WRONGTYPE");
                                    }
                                    return entry;
                                })
                        .value();
    }

    public RedisList getList(String key) {
        Entry entry = get(key);
        if (entry == null) return null;

        if (!(entry.value() instanceof RedisList list)) {
            throw new IllegalArgumentException("WRONGTYPE");
        }
        return list;
    }

    // ---------------------------------------------------------
    // HASHES
    // ---------------------------------------------------------

    public RedisHash getOrCreateHash(String key) {
        removeIfExpired(key);
        return (RedisHash) data.compute(key,
                                (k, entry) -> {
                                    if (entry == null) {
                                        return new Entry(new RedisHash());
                                    }
                                    if (!(entry.value() instanceof RedisHash)) {
                                        throw new IllegalArgumentException("WRONGTYPE");
                                    }
                                    return entry;
                                })
                        .value();
    }

    public RedisHash getHash(String key) {
        Entry entry = get(key);
        if (entry == null) return null;

        if (!(entry.value() instanceof RedisHash hash)) {
            throw new IllegalArgumentException("WRONGTYPE");
        }
        return hash;
    }

    // ---------------------------------------------------------
    // SETS
    // ---------------------------------------------------------

    public RedisSet getOrCreateSet(String key) {
        removeIfExpired(key);
        return (RedisSet) data.compute(key,
                                (k, entry) -> {
                                    if (entry == null) {
                                        return new Entry(new RedisSet());
                                    }
                                    if (!(entry.value() instanceof RedisSet)) {
                                        throw new IllegalArgumentException("WRONGTYPE");
                                    }
                                    return entry;
                                })
                        .value();
    }

    public RedisSet getSet(String key) {
        Entry entry = get(key);
        if (entry == null) return null;

        if (!(entry.value() instanceof RedisSet set)) {
            throw new IllegalArgumentException("WRONGTYPE");
        }
        return set;
    }
}