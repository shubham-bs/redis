package redis.storage;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RedisListTest {

    @Test
    void shouldPushValuesToLeft() {

        RedisList list = new RedisList();

        list.pushLeft("apple");
        list.pushLeft("banana");

        assertEquals("[banana, apple]", list.toString());
    }

    @Test
    void shouldPushValuesToRight() {

        RedisList list = new RedisList();

        list.pushLeft("apple");
        list.pushLeft("banana");
        list.pushRight("mango");

        assertEquals("[banana, apple, mango]", list.toString());
    }

    @Test
    void shouldPopValueFromLeft() {

        RedisList list = new RedisList();

        list.pushRight("apple");
        list.pushRight("banana");

        assertEquals("apple", list.popLeft());

        assertEquals("[banana]", list.toString());
    }

    @Test
    void shouldPopValueFromRight() {

        RedisList list = new RedisList();

        list.pushRight("apple");
        list.pushRight("banana");

        assertEquals("banana", list.popRight());

        assertEquals("[apple]", list.toString());
    }

    @Test
    void shouldReturnNullWhenPoppingEmptyList() {

        RedisList list = new RedisList();

        assertNull(list.popLeft());
        assertNull(list.popRight());
    }

    @Test
    void shouldReturnListSize() {

        RedisList list = new RedisList();

        assertEquals(0, list.size());

        list.pushRight("apple");
        list.pushRight("banana");
        list.pushRight("mango");

        assertEquals(3, list.size());

        list.popLeft();

        assertEquals(2, list.size());
    }

    @Test
    void shouldReturnRange() {

        RedisList list = new RedisList();

        list.pushRight("apple");
        list.pushRight("banana");
        list.pushRight("mango");
        list.pushRight("orange");

        assertEquals(List.of("banana", "mango"), list.range(1, 2));
    }

    @Test
    void shouldSupportNegativeIndexes() {

        RedisList list = new RedisList();

        list.pushRight("apple");
        list.pushRight("banana");
        list.pushRight("mango");
        list.pushRight("orange");

        assertEquals(List.of("mango", "orange"), list.range(-2, -1));
    }

    @Test
    void shouldClampRangeBeyondListBoundaries() {

        RedisList list = new RedisList();

        list.pushRight("apple");
        list.pushRight("banana");

        assertEquals(List.of("apple", "banana"), list.range(-100, 100));
    }

    @Test
    void shouldReturnEmptyRangeWhenStartIsAfterStop() {

        RedisList list = new RedisList();

        list.pushRight("apple");
        list.pushRight("banana");

        assertEquals(List.of(), list.range(1, 0));
    }

    @Test
    void shouldReturnEmptyRangeForEmptyList() {

        RedisList list = new RedisList();

        assertEquals(List.of(), list.range(0, 10));
    }
}