package org.example.interfaces;

import java.util.Set;

import org.example.exception.LoggerException;

public interface LogFactory {

	Log open(long logId) throws LoggerException;

	Set<LogCursor> initializeCursors(Log log) throws LoggerException;
}
