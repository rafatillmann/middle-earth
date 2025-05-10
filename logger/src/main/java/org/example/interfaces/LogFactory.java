package org.example.interfaces;

import org.example.exception.LoggerException;

public interface LogFactory {

    Writer getWriter(long logId) throws LoggerException;
}
