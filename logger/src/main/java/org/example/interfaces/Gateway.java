package org.example.interfaces;

import org.example.exception.LoggerException;

public interface Gateway {
    void initialize() throws LoggerException;

    void close();
}
