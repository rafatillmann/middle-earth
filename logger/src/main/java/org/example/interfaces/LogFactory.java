package org.example.interfaces;

import org.example.exception.LoggerException;

import java.util.Set;

public interface LogFactory {

    Log open(long logId) throws LoggerException;

    Set<Cursor> getCursors(Log log) throws LoggerException;
}
