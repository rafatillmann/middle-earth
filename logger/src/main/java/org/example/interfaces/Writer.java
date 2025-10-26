package org.example.interfaces;

import org.example.exception.LoggerException;

public interface Writer {
    void write(byte[] data) throws LoggerException;

    void write(byte[] data, LogCallback.AddEntryCallback callback) throws LoggerException;

    Reader getReader() throws LoggerException;
}
