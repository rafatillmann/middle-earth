package org.example.memory;

import org.example.exception.LoggerException;
import org.example.interfaces.Entry;
import org.example.interfaces.Reader;

import java.util.List;

public class InMemoryReader implements Reader {

    private final InMemoryLog inMemoryLog;

    public InMemoryReader(InMemoryLog inMemoryLog) {
        this.inMemoryLog = inMemoryLog;
    }

    @Override
    public Entry read(long entryId) throws LoggerException {
        InMemoryEntry entry = inMemoryLog.getEntry(entryId);
        if (entry == null) {
            throw new LoggerException("Entry with ID " + entryId + " not found");
        }
        return entry;
    }

    @Override
    public List<Entry> read(long firstEntryId, long lastEntryId) throws LoggerException {
        if (firstEntryId > lastEntryId) {
            throw new LoggerException("First entry ID (" + firstEntryId + ") cannot be greater than last entry ID (" + lastEntryId + ")");
        }
        
        List<InMemoryEntry> entries = inMemoryLog.getEntries(firstEntryId, lastEntryId);
        return entries.stream().map(entry -> (Entry) entry).toList();
    }
}
