package org.example.interfaces;

import java.util.List;

public interface Log {

	void write(byte[] data) throws Exception;

	LogEntry read(long id) throws Exception;

	List<LogEntry> read(long firstEntryId, long lastEntryId) throws Exception;

}
