package org.example.interfaces;

import org.example.exception.LoggerException;

import java.util.List;

public interface Log {

    void write(byte[] data) throws LoggerException;

    Entry read(long entryId) throws LoggerException;

    List<Entry> read(long firstEntryId, long lastEntryId) throws LoggerException;

}
