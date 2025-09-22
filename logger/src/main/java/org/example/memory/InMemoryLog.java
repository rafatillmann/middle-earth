package org.example.memory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryLog {

    private final Cache<Long, InMemoryEntry> cache;
    private final AtomicLong currentId;
    private final ExecutorService executorService;

    public InMemoryLog() {
        this.cache = CacheBuilder.newBuilder().build();
        this.currentId = new AtomicLong(0L);
        this.executorService = Executors.newFixedThreadPool(8);
    }

    public CompletableFuture<Long> putAsync(byte[] data) {
        return CompletableFuture.supplyAsync(() -> {
            long entryId = currentId.incrementAndGet();
            var newEntry = new InMemoryEntry(entryId, data);
            cache.put(entryId, newEntry);
            return entryId;
        }, executorService);
    }

    public CompletableFuture<List<InMemoryEntry>> getEntriesAsync(Long startKey, Long endKey) {
        return CompletableFuture.supplyAsync(() -> {
            List<InMemoryEntry> result = new ArrayList<>();
            for (long i = startKey; i <= endKey; i++) {
                InMemoryEntry entry = cache.getIfPresent(i);
                if (entry != null) {
                    result.add(entry);
                }
            }
            return result;
        }, executorService);
    }
}
