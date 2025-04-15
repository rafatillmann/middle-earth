package org.example.interfaces;

import org.example.exception.LoggerException;

public interface LogFactory {

    Log open(long logId) throws LoggerException;
}
