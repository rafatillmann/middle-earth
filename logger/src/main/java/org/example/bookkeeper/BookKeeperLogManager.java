package org.example.bookkeeper;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.bookkeeper.client.api.BKException;
import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.bookkeeper.client.api.ReadHandle;
import org.apache.bookkeeper.client.api.WriteHandle;
import org.example.exception.LoggerException;
import org.example.interfaces.Cursor;
import org.example.interfaces.Entry;
import org.example.interfaces.LogCallback.AddEntryCallback;
import org.example.interfaces.LogManager;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Slf4j
public class BookKeeperLogManager implements LogManager {

    private final long logId;
    private final BookKeeper bookKeeper;

    private final ConcurrentHashMap<Long, Socket> clientsToReply = new ConcurrentHashMap<>();

    private Set<Cursor> activeCursors;
    private WriteHandle writer;
    private ReadHandle reader;

    // TODO - Current Ledger will be necessary when implementing single log abstraction
    private long currentLedgerId;

    public BookKeeperLogManager(long logId, BookKeeper bookKeeper) throws LoggerException {
        // TODO - Add metadata to zookkeeper to recovery if writer was crashed
        this.logId = logId;
        this.bookKeeper = bookKeeper;
        this.writer = writer();
        //this.reader = reader();
    }

    public void initializeCursors(Set<Cursor> activeCursors) {
        this.activeCursors = activeCursors;
    }

    @Override
    public long write(byte[] data) throws LoggerException {
        try {
            return write(data, this::notifyAddEntry);
        } catch (BKException | InterruptedException | ExecutionException e) {
            throw new LoggerException("Unable to append data", e);
        }
    }

    @Override
    public long write(byte[] data, Socket socketToReply) throws LoggerException {
        try {
            return write(data, entryId -> {
                addClientToReply(entryId, socketToReply);
                notifyAddEntry(entryId);
            });
        } catch (BKException | InterruptedException | ExecutionException e) {
            throw new LoggerException("Unable to append data", e);
        }
    }

    public long write(byte[] data, AddEntryCallback callback) throws BKException, InterruptedException, ExecutionException {
        return writer.appendAsync(data)
                .thenApplyAsync(entryId -> {
                    callback.onComplete(entryId);
                    return entryId;
                }).get();
    }

    @Override
    public Entry read(long entryId) throws LoggerException {
        try {
            return writer.readAsync(entryId, entryId)
                    .thenApply(ledgerEntries -> ledgerEntries.getEntry(entryId))
                    .thenApply(ledgerEntry -> new BookKeeperEntry(ledgerEntry.getLedgerId(), ledgerEntry.getEntryId(), ledgerEntry.getEntryBytes()))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new LoggerException("Unable to read data", e);
        }
    }

    @Override
    public List<Entry> read(long firstEntryId, long lastEntryId) throws LoggerException {
        try {
            return writer.readAsync(firstEntryId, lastEntryId)
                    .thenApply(ledgerEntries -> {
                        List<Entry> result = new ArrayList<>();
                        ledgerEntries.forEach(entry -> result.add(new BookKeeperEntry(entry.getLedgerId(), entry.getEntryId(), entry.getEntryBytes())));
                        return result;
                    }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new LoggerException("Unable to read data", e);
        }

    }

    @Override
    public void addClientToReply(long entryId, Socket socket) {
        clientsToReply.put(entryId, socket);
    }

    @Override
    public Socket getClientToReply(long entryId) {
        return clientsToReply.remove(entryId);
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

    // TODO - Use read handle instance when create single log abstraction
    private ReadHandle reader() throws LoggerException {
        try {
            return bookKeeper.newOpenLedgerOp()
                    .withLedgerId(currentLedgerId)
                    .withPassword("middle-earth".getBytes())
                    .execute()
                    .get();
        } catch (ExecutionException | InterruptedException e) {
            throw new LoggerException("Unable to create a new read handle", e);
        }
    }

    private Map<String, byte[]> createLedgerCustomMetadata(long logId) {
        return ImmutableMap.<String, byte[]>builder()
                .put("logId", Long.toString(logId).getBytes())
                .build();
    }

    private void notifyAddEntry(Long entryId) {
        for (Cursor activeCursor : activeCursors) {
            try {
                activeCursor.notifyCursor(entryId);
            } catch (LoggerException e) {
                log.warn("Unable to notify cursors", e);
            }
        }
    }
}
