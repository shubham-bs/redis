package redis.command.handlers;

import org.junit.jupiter.api.Test;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisString;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TtlCommandTest {

    @Test
    void returnsMinusTwoForMissingKey() {

        TtlCommand command =
                new TtlCommand();

        RespValue response =
                command.execute(
                        new DataStore(),
                        new CommandRequest(
                                "TTL",
                                List.of("unknown")
                        )
                );

        assertEquals(
                new RespValue.IntegerValue(-2),
                response
        );
    }

    @Test
    void returnsMinusOneForPersistentKey() {

        DataStore store =
                new DataStore();

        store.set(
                "name",
                new RedisString("shubham")
        );

        TtlCommand command =
                new TtlCommand();

        RespValue response =
                command.execute(
                        store,
                        new CommandRequest(
                                "TTL",
                                List.of("name")
                        )
                );

        assertEquals(
                new RespValue.IntegerValue(-1),
                response
        );
    }

    @Test
    void returnsPositiveTtl() {

        DataStore store =
                new DataStore();

        store.set(
                "name",
                new RedisString("shubham")
        );

        store.expire(
                "name",
                10
        );

        TtlCommand command =
                new TtlCommand();

        RespValue response =
                command.execute(
                        store,
                        new CommandRequest(
                                "TTL",
                                List.of("name")
                        )
                );

        long ttl =
                ((RespValue.IntegerValue) response)
                        .value();

        assertTrue(
                ttl >= 0 && ttl <= 10
        );
    }
}