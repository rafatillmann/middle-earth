package org.example.bookkeeper;

import org.example.interfaces.LogEntry;

public class LedgerEntry implements LogEntry {

	private final long entryId;
	private final byte[] payload;

	public LedgerEntry(long entryId, byte[] payload) {
		this.entryId = entryId;
		this.payload = payload;
	}

	@Override public byte[] getPayload() {
		return payload;
	}

	@Override public long getEntryId() {
		return entryId;
	}
}
