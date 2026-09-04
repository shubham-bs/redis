package redis.storage;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RedisSetTest {

    @Test
    void shouldAddMember() {

        RedisSet set = new RedisSet();

        assertTrue(set.add("java"));
        assertTrue(set.contains("java"));
    }

    @Test
    void shouldNotAddDuplicateMember() {

        RedisSet set = new RedisSet();

        assertTrue(set.add("java"));
        assertFalse(set.add("java"));
        assertEquals(1, set.size());
    }

    @Test
    void shouldRemoveMember() {

        RedisSet set = new RedisSet();

        set.add("java");

        assertTrue(set.remove("java"));
        assertFalse(set.contains("java"));
    }

    @Test
    void shouldCheckMembership() {

        RedisSet set = new RedisSet();

        set.add("java");

        assertTrue(set.contains("java"));
        assertFalse(set.contains("python"));
    }

    @Test
    void shouldReturnMembers() {

        RedisSet set = new RedisSet();

        set.add("java");
        set.add("spring");

        assertEquals(Set.of("java", "spring"), set.members());
    }
}