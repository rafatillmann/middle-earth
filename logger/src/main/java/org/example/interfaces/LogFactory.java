package org.example.interfaces;

import org.example.exception.LoggerException;

public interface LogFactory {

    public Log createLog(long logId) throws LoggerException;
}
