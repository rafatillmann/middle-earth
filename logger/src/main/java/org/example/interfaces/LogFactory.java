package org.example.interfaces;

import org.example.exception.LoggerException;

public interface LogFactory {

    public Log open(long logId) throws LoggerException;
}
