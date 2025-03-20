package org.example.bookkeeper;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.bookkeeper.client.api.BKException;
import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.bookkeeper.client.api.ReadHandle;
import org.apache.bookkeeper.client.api.WriteHandle;
import org.example.exception.LoggerException;
import org.example.interfaces.Log;
import org.example.interfaces.LogCallback.AddEntryCallback;

import com.google.common.collect.ImmutableMap;

public class LedgerLog implements Log {

	private final long logId;
	private final BookKeeper bookKeeper;
	private WriteHandle writer;
	private ReadHandle reader;

	public LedgerLog(long logId, BookKeeper bookKeeper) throws LoggerException {
		// TODO - Initialize cursor LedgerLog
		this.logId = logId;
		this.bookKeeper = bookKeeper;
		this.writer = writer();
	}

	@Override
	public void write(byte[] data) throws Exception {
		try {
			write(data, new AddEntryCallback() {
				@Override public void onComplete() {

				}
			});
		} catch (BKException | InterruptedException e) {
			throw new LoggerException("Unable to append data", e);
		}
	}

	public void write(byte[] data, AddEntryCallback callback) throws BKException, InterruptedException {
		writer.appendAsync(data)
				.thenAcceptAsync(entryId -> {
					callback.onComplete();
				});
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
}
