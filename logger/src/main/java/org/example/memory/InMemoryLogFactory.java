package org.example.memory;

import org.example.exception.LoggerException;
import org.example.interfaces.LogFactory;
import org.example.interfaces.Writer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryLogFactory implements LogFactory {

    private final ConcurrentMap<Long, InMemoryLog> logs = new ConcurrentHashMap<>();

    @Override
    public Writer getWriter(long logId) throws LoggerException {
        // Get or create a log for the given logId
        InMemoryLog log = logs.computeIfAbsent(logId, id -> new InMemoryLog());
        return new InMemoryWriter(log);
    }
}
