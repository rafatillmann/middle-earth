package org.example.cursor;

import org.example.interfaces.LogCursor;

public class BookKeeperLogCursor implements LogCursor {

	private final String cursorName;

	public BookKeeperLogCursor(String cursorName) {
		this.cursorName = cursorName;
	}

	@Override public byte[] read() throws Exception {
		return new byte[0];
	}
}
