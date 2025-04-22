package org.example.interfaces;

import org.example.exception.LoggerException;

import java.util.List;

public interface Reader {
    Entry read(long entryId) throws LoggerException;

    List<Entry> read(long firstEntryId, long lastEntryId) throws LoggerException;
}
