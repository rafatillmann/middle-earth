package org.example.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryLog {

    private final ConcurrentSkipListMap<Long, InMemoryEntry> entries;
    private final AtomicLong currentId;

    public InMemoryLog() {
        this.entries = new ConcurrentSkipListMap<>();
        this.currentId = new AtomicLong(0L);
    }

    public Long put(byte[] data) {
        long entryId = currentId.incrementAndGet();
        var newEntry = new InMemoryEntry(entryId, data);

        entries.put(entryId, newEntry);

        return entryId;
    }

    public InMemoryEntry getEntry(Long entryId) {
        return entries.get(entryId);
    }

    public List<InMemoryEntry> getEntries(Long startKey, Long endKey) {
        return new ArrayList<>(entries.subMap(startKey, true, endKey, true).values());
    }
}
