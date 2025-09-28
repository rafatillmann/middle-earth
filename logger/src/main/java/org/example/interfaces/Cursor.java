package org.example.interfaces;

import org.example.exception.LoggerException;

import java.util.concurrent.CompletableFuture;

public interface Cursor {
    CompletableFuture<String> entryAvailable(long entryId) throws LoggerException;

    void close() throws LoggerException;
}
