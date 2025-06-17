package io.github.songminkyu.account;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

public class CompletableFutureTest {

    @Test
    void testsCompletableFuture() throws ExecutionException, InterruptedException {
       var future1 = CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate a long-running operation
                Thread.sleep(2000); // 2 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Result of Future 1";
        });

       var future2 = CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate another long-running operation
                Thread.sleep(1500); // 1.5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Result of Future 2";
        });

       var combinedFuture = CompletableFuture.allOf(future1, future2);

        combinedFuture.join();

        // Block and wait for all futures to complete
        combinedFuture.join();
        var result1 = future1.get();
        var result2 = future1.get();
        assertNotNull(result1);
        assertNotNull(result2);
    }
}
