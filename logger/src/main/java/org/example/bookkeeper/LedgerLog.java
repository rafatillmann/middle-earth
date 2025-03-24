package org.example.bookkeeper;

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

import com.google.common.collect.ImmutableMap;

public class LedgerLog implements Log {

	private final long logId;
	private final BookKeeper bookKeeper;
	private final Set<LogCursor> cursors;

	private WriteHandle writer;
	private ReadHandle reader;

	public LedgerLog(long logId, BookKeeper bookKeeper, Set<LogCursor> cursors) throws LoggerException {
		// TODO - Add metadata to zookkeeper to recovery if writer was crashed
		this.logId = logId;
		this.bookKeeper = bookKeeper;
		this.cursors = cursors;
		this.writer = writer();
	}

	@Override
	public void write(byte[] data) throws Exception {
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
	public byte[] read(long id) throws Exception {
		try {
			var entry = reader.read(id, id).getEntry(id);
			return entry.getEntryBytes();
		} catch (BKException | InterruptedException e) {
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

	private Map<String, byte[]> createLedgerCustomMetadata(long logId) {
		return ImmutableMap.<String, byte[]>builder()
				.put("logId", Long.toString(logId).getBytes())
				.build();
	}

	private void notifyAddEntry(Long entryId) {
		for (LogCursor cursor : cursors) {
			// TODO - Add logic to notify cursors to read entries and send to replicas
			System.out.println("Notify cursors" + entryId);
		}
	}
}
