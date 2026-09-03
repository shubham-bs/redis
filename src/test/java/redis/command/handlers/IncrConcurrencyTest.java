package redis.command.handlers;

import org.junit.jupiter.api.Test;
import redis.command.CommandRequest;
import redis.protocol.RespValue;
import redis.storage.DataStore;
import redis.storage.RedisString;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IncrConcurrencyTest {

    @Test
    void concurrentIncrements() throws InterruptedException {

        DataStore store = new DataStore();

        store.set(
                "counter",
                new RedisString("0")
        );

        IncrCommand command = new IncrCommand();

        int numberOfThreads = 100;

        CountDownLatch start =
                new CountDownLatch(1);

        CountDownLatch done =
                new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {

            Thread.ofVirtual().start(() -> {

                try {

                    start.await();

                    command.execute(
                            store,
                            new CommandRequest(
                                    "INCR",
                                    List.of("counter")
                            )
                    );

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                } finally {

                    done.countDown();
                }
            });
        }

        start.countDown();

        done.await();

        RedisString result =
                (RedisString) store.get("counter").value();

        long finalValue =
                Long.parseLong(result.value());

        assertEquals(
                numberOfThreads,
                finalValue
        );
    }
}