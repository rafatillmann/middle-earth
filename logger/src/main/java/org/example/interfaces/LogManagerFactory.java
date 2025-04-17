package org.example.interfaces;

import org.example.exception.LoggerException;

public interface LogManagerFactory {

    LogManager open(long logId) throws LoggerException;
}
