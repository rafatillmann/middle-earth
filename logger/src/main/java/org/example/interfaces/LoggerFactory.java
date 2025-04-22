package org.example.interfaces;

import org.example.exception.LoggerException;

public interface LoggerFactory {

    Logger open(long logId) throws LoggerException;
}
