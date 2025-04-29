package org.example.interfaces;

import org.example.exception.LoggerException;

public interface Logger {
    long write(byte[] data) throws LoggerException;

    long write(byte[] data, LogCallback.AddEntryCallback callback) throws LoggerException;

    Reader getReader() throws LoggerException;
}
