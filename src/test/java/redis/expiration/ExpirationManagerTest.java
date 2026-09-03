package redis.expiration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpirationManagerTest {

    @Test
    void keyIsNotExpiredBeforeDeadline() {
        ExpirationManager manager = new ExpirationManager();

        manager.setExpiration("name", System.currentTimeMillis() + 10_000);

        assertFalse(manager.isExpired("name"));
    }

    @Test
    void keyIsExpiredAfterDeadline() {
        ExpirationManager manager = new ExpirationManager();

        manager.setExpiration("name", System.currentTimeMillis() - 1);

        assertTrue(manager.isExpired("name"));
    }

    @Test
    void missingExpirationMeansNotExpired() {
        ExpirationManager manager = new ExpirationManager();

        assertFalse(manager.isExpired("unknown"));
    }
}