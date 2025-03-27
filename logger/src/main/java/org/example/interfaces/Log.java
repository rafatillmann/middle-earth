package org.example.interfaces;

import java.util.List;

import org.example.exception.LoggerException;

public interface Log {

	void write(byte[] data) throws LoggerException;

	LogEntry read(long id) throws LoggerException;

	List<LogEntry> read(long firstEntryId, long lastEntryId) throws LoggerException;

}
