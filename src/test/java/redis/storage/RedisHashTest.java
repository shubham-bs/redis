package redis.storage;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RedisHashTest {

    @Test
    void shouldAddNewField() {

        RedisHash hash = new RedisHash();

        assertTrue(hash.set("name", "Shubham"));
        assertEquals("Shubham", hash.get("name"));
    }

    @Test
    void shouldReturnFalseWhenUpdatingExistingField() {

        RedisHash hash = new RedisHash();

        assertTrue(hash.set("name", "Shubham"));
        assertFalse(hash.set("name", "Rahul"));
        assertEquals("Rahul", hash.get("name"));
    }

    @Test
    void shouldCheckFieldExistence() {

        RedisHash hash = new RedisHash();

        hash.set("name", "Shubham");

        assertTrue(hash.exists("name"));
        assertFalse(hash.exists("age"));
    }

    @Test
    void shouldDeleteField() {

        RedisHash hash = new RedisHash();

        hash.set("name", "Shubham");

        assertTrue(hash.delete("name"));
        assertFalse(hash.exists("name"));
    }

    @Test
    void shouldReturnAllFields() {

        RedisHash hash = new RedisHash();

        hash.set("name", "Shubham");
        hash.set("city", "Nagpur");

        assertEquals(Map.of(
                        "name", "Shubham",
                        "city", "Nagpur"),
                hash.all());
    }
}