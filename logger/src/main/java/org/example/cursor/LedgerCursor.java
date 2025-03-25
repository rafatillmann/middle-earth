package org.example.cursor;

import org.example.interfaces.LogCursor;

public class LedgerCursor implements LogCursor {

	private final long logId;
	private final String name;
	private final String uri;

	public LedgerCursor(long logId, String name, String uri) {
		this.logId = logId;
		this.name = name;
		this.uri = uri;
	}

	@Override
	public byte[] read() throws Exception {
		return new byte[0];
	}
}
