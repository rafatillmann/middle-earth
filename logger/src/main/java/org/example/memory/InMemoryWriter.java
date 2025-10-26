package org.example.memory;

import org.example.exception.LoggerException;
import org.example.interfaces.LogCallback;
import org.example.interfaces.Reader;
import org.example.interfaces.Writer;

public class InMemoryWriter implements Writer {

    private final InMemoryLog inMemoryLog;

    public InMemoryWriter(InMemoryLog inMemoryLog) {
        this.inMemoryLog = inMemoryLog;
    }

    @Override
    public void write(byte[] data) throws LoggerException {
        inMemoryLog.put(data);
    }

    @Override
    public void write(byte[] data, LogCallback.AddEntryCallback callback) throws LoggerException {
        var entryId = inMemoryLog.put(data);
        callback.onComplete(entryId);
    }

    @Override
    public Reader getReader() throws LoggerException {
        return new InMemoryReader(inMemoryLog);
    }
}
