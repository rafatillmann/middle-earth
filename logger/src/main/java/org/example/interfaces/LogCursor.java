package org.example.interfaces;

import org.example.exception.LoggerException;

public interface LogCursor {

	void notifyCursor(long entryId) throws LoggerException;
}
