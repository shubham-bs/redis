package redis.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataStoreTest {

    @Test
    void storesAndRetrievesValue() {
        DataStore store = new DataStore();

        store.set("name", new RedisString("Shubham"));

        Entry entry = store.get("name");

        assertNotNull(entry);
        assertInstanceOf(RedisString.class, entry.value());

        RedisString value = (RedisString) entry.value();

        assertEquals("Shubham", value.value());
    }

    @Test
    void returnsNullForMissingKey() {
        DataStore store = new DataStore();
        assertNull(store.get("does-not-exist"));
    }

    @Test
    void deletesExistingKey() {
        DataStore store = new DataStore();
        store.set("name", new RedisString("Shubham"));

        assertTrue(store.exists("name"));

        assertTrue(store.delete("name"));

        assertFalse(store.exists("name"));
        assertNull(store.get("name"));
    }

    @Test
    void deletingMissingKeyReturnsFalse() {
        DataStore store = new DataStore();
        assertFalse(
                store.delete("does-not-exist")
        );
    }

    @Test
    void existsChecksKeyPresence() {

        DataStore store = new DataStore();

        assertFalse(store.exists("name"));

        store.set("name", new RedisString("Shubham"));

        assertTrue(store.exists("name"));
    }

    @Test
    void setReplacesExistingValue() {

        DataStore store = new DataStore();

        store.set("name", new RedisString("Shubham"));

        store.set("name", new RedisString("Alice"));

        RedisString value = (RedisString) store.get("name").value();

        assertEquals("Alice", value.value());
    }
}