package org.example.interfaces;

import org.example.exception.LoggerException;

public interface Cursor {

    void notifyCursor(long entryId) throws LoggerException;
}
