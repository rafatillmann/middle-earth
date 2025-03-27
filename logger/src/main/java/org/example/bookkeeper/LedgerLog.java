package org.example.bookkeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.bookkeeper.client.api.BKException;
import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.bookkeeper.client.api.ReadHandle;
import org.apache.bookkeeper.client.api.WriteHandle;
import org.example.exception.LoggerException;
import org.example.interfaces.Log;
import org.example.interfaces.LogCallback.AddEntryCallback;
import org.example.interfaces.LogCursor;
import org.example.interfaces.LogEntry;

import com.google.common.collect.ImmutableMap;

public class LedgerLog implements Log {

	private final long logId;
	private final BookKeeper bookKeeper;

	private Set<LogCursor> activeCursors;
	private WriteHandle writer;
	private ReadHandle reader;

	public LedgerLog(long logId, BookKeeper bookKeeper) throws LoggerException {
		// TODO - Add metadata to zookkeeper to recovery if writer was crashed
		this.logId = logId;
		this.bookKeeper = bookKeeper;
		this.writer = writer();
		this.reader = reader();
	}

	public void initialize(Set<LogCursor> activeCursors) {
		this.activeCursors = activeCursors;
	}

	@Override
	public void write(byte[] data) throws LoggerException {
		try {
			write(data, entryId -> notifyAddEntry(entryId));
		} catch (BKException | InterruptedException e) {
			throw new LoggerException("Unable to append data", e);
		}
	}

	public void write(byte[] data, AddEntryCallback callback) throws BKException, InterruptedException {
		writer.appendAsync(data).thenAcceptAsync(entryId -> callback.onComplete(entryId));
	}

	@Override
	public LogEntry read(long entryId) throws LoggerException {
		try {
			return reader.readAsync(entryId, entryId)
					.thenApply(ledgerEntries -> ledgerEntries.getEntry(entryId))
					.thenApply(ledgerEntry -> new LedgerEntry(ledgerEntry.getEntryId(), ledgerEntry.getEntryBytes()))
					.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new LoggerException("Unable to read data", e);
		}
	}

	// TODO - Use iterable
	@Override
	public List<LogEntry> read(long firstEntryId, long lastEntryId) throws LoggerException {
		try {
			return reader.readAsync(firstEntryId, lastEntryId)
					.thenApply(ledgerEntries -> {
						List<LogEntry> result = new ArrayList<>();
						ledgerEntries.forEach(entry -> result.add(new LedgerEntry(entry.getEntryId(), entry.getEntryBytes())));
						return result;
					}).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new LoggerException("Unable to read data", e);
		}

	}

	private WriteHandle writer() throws LoggerException {
		try {
			return bookKeeper.newCreateLedgerOp()
					.withEnsembleSize(3)
					.withWriteQuorumSize(2)
					.withAckQuorumSize(2)
					.withPassword("middle-earth".getBytes())
					.withCustomMetadata(createLedgerCustomMetadata(this.logId))
					.execute()
					.get();
		} catch (ExecutionException | InterruptedException e) {
			throw new LoggerException("Unable to create new ledger", e);
		}
	}

	private ReadHandle reader() {

	}

	private Map<String, byte[]> createLedgerCustomMetadata(long logId) {
		return ImmutableMap.<String, byte[]>builder()
				.put("logId", Long.toString(logId).getBytes())
				.build();
	}

	private void notifyAddEntry(Long entryId) {
		for (LogCursor activeCursor : activeCursors) {
			try {
				activeCursor.notifyCursor(entryId);
			} catch (LoggerException e) {
				e.printStackTrace();
			}
		}
	}
}
