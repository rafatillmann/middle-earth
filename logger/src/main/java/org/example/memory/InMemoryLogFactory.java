package org.example.memory;

import org.example.exception.LoggerException;
import org.example.interfaces.LogFactory;
import org.example.interfaces.Writer;

public class InMemoryLogFactory implements LogFactory {
    @Override
    public Writer getWriter(long logId) throws LoggerException {
        InMemoryLog inMemoryLog = new InMemoryLog();
        return new InMemoryWriter(inMemoryLog);
    }
}
