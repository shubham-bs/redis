package redis.expiration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExpirationManager {

    private final Map<String, Long> expirationTimes = new ConcurrentHashMap<>();

    public void setExpiration(String key, long expirationTimeMillis) {
        expirationTimes.put(key, expirationTimeMillis);
    }

    public void removeExpiration(String key) {
        expirationTimes.remove(key);
    }

    public Long getExpiration(String key) {
        return expirationTimes.get(key);
    }

    public boolean isExpired(String key) {
        Long expirationTime = expirationTimes.get(key);

        if (expirationTime == null) return false;

        return System.currentTimeMillis() >= expirationTime;
    }
}