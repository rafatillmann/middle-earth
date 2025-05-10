package org.example.bookkeeper;

import com.google.common.collect.ImmutableMap;
import org.apache.bookkeeper.client.api.BKException;
import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.bookkeeper.client.api.WriteHandle;
import org.example.exception.LoggerException;
import org.example.interfaces.LogCallback.AddEntryCallback;
import org.example.interfaces.Reader;
import org.example.interfaces.Writer;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class BookKeeperWriter implements Writer {

    private final long logId;
    private final BookKeeper bookKeeper;

    private WriteHandle writer;
    // TODO - Current Ledger will be necessary when implementing single log abstraction
    private long currentLedgerId;

    public BookKeeperWriter(long logId, BookKeeper bookKeeper) throws LoggerException {
        // TODO - Add metadata to zookkeeper to recovery if writer was crashed
        this.logId = logId;
        this.bookKeeper = bookKeeper;
        this.writer = writer();
    }

    @Override
    public long write(byte[] data) throws LoggerException {
        try {
            return writer.append(data);
        } catch (InterruptedException | BKException e) {
            throw new LoggerException("Unable to append data", e);
        }
    }

    @Override
    public long write(byte[] data, AddEntryCallback callback) throws LoggerException {
        try {
            return writer.appendAsync(data)
                    .thenApplyAsync(entryId -> {
                        callback.onComplete(entryId);
                        return entryId;
                    }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new LoggerException("Unable to append data", e);
        }
    }

    @Override
    public Reader getReader() throws LoggerException {
        return new BookKeeperReader(logId, bookKeeper, writer.getLedgerMetadata());
    }

    private WriteHandle writer() throws LoggerException {
        try {
            return bookKeeper.newCreateLedgerOp()
                    .withEnsembleSize(3)
                    .withWriteQuorumSize(2)
                    .withAckQuorumSize(2)
                    .withPassword("middle-earth".getBytes())
                    .withCustomMetadata(createLedgerCustomMetadata(logId))
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
