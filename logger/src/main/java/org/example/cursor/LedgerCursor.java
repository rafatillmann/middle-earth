package org.example.cursor;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;

import org.example.exception.LoggerException;
import org.example.interfaces.Log;
import org.example.interfaces.LogCursor;
import org.example.interfaces.LogEntry;

public class LedgerCursor implements LogCursor {

	private final Log log;
	private final String name;
	private final URI uri;

	// TODO - Add lastEntryId to zookeeper
	private long lastEntryId = 0;

	public LedgerCursor(Log log, String name, URI uri) {
		this.log = log;
		this.name = name;
		this.uri = uri;
	}

	@Override public void notifyCursor(long entryId) throws LoggerException {
		try {
			Socket serverSocket = new Socket(uri.getHost(), uri.getPort());

			//BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream(), true);

			// TODO - Verify if entryId was equal or greater of lastAddConfirmed
			var pendingEntries = log.read(lastEntryId, entryId);
			for (LogEntry entry : pendingEntries) {
				serverOut.println(new String(entry.getPayload(), UTF_8));
				// TODO - Check for a better way to update the lastEntryId
				lastEntryId = entry.getEntryId();
			}

			serverSocket.close();

		} catch (IOException e) {
			throw new LoggerException("Unable to send entries to server", e);
		}
	}
}
