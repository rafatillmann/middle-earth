package org.example.interfaces;

import org.example.exception.LoggerException;

public interface Cursor {
    void entryAvailable(long entryId) throws LoggerException;
}
