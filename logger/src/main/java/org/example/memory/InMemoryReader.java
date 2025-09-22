package org.example.memory;

import org.example.exception.LoggerException;
import org.example.interfaces.Entry;
import org.example.interfaces.Reader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class InMemoryReader implements Reader {

    private final InMemoryLog inMemoryLog;

    public InMemoryReader(InMemoryLog inMemoryLog) {
        this.inMemoryLog = inMemoryLog;
    }

    @Override
    public Entry read(long entryId) throws LoggerException {
        return null;
    }

    @Override
    public List<Entry> read(long firstEntryId, long lastEntryId) throws LoggerException {
        try {
            return inMemoryLog.getEntriesAsync(firstEntryId, lastEntryId)
                    .thenApply(inMemoryEntries -> (List<Entry>) new ArrayList<Entry>(inMemoryEntries)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new LoggerException("Unable to read data", e);
        }
    }
}
