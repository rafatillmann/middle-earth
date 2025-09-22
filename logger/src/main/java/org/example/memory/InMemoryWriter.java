package org.example.memory;

import org.example.exception.LoggerException;
import org.example.interfaces.LogCallback;
import org.example.interfaces.Reader;
import org.example.interfaces.Writer;

import java.util.concurrent.ExecutionException;

public class InMemoryWriter implements Writer {

    private final InMemoryLog inMemoryLog;

    public InMemoryWriter(InMemoryLog inMemoryLog) {
        this.inMemoryLog = inMemoryLog;
    }

    @Override
    public long write(byte[] data) throws LoggerException {
        return 0;
    }

    @Override
    public long write(byte[] data, LogCallback.AddEntryCallback callback) throws LoggerException {
        try {
            return inMemoryLog.putAsync(data)
                    .thenApplyAsync(entryId -> {
                        callback.onComplete(entryId);
                        return entryId;
                    }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new LoggerException("Unable to append data", e);
        }
    }

    @Override
    public Reader getReader() throws LoggerException {
        return new InMemoryReader(inMemoryLog);
    }
}
