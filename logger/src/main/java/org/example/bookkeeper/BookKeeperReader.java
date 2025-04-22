package org.example.bookkeeper;

import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.bookkeeper.client.api.LedgerMetadata;
import org.apache.bookkeeper.client.api.ReadHandle;
import org.example.exception.LoggerException;
import org.example.interfaces.Entry;
import org.example.interfaces.Reader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BookKeeperReader implements Reader {

    private final long logId;
    private final BookKeeper bookKeeper;
    private final LedgerMetadata ledgerMetadata;

    private ReadHandle currentReader;

    public BookKeeperReader(long logId, BookKeeper bookKeeper, LedgerMetadata ledgerMetadata) throws LoggerException {
        this.logId = logId;
        this.bookKeeper = bookKeeper;
        this.ledgerMetadata = ledgerMetadata;
        this.currentReader = reader();
    }

    @Override
    public Entry read(long entryId) throws LoggerException {
        try {
            return currentReader.readUnconfirmedAsync(entryId, entryId)
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
            // TODO - Read just confirmed entries
            return currentReader.readUnconfirmedAsync(firstEntryId, lastEntryId)
                    .thenApply(ledgerEntries -> {
                        List<Entry> result = new ArrayList<>();
                        ledgerEntries.forEach(entry -> result.add(new BookKeeperEntry(entry.getLedgerId(), entry.getEntryId(), entry.getEntryBytes())));
                        return result;
                    }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new LoggerException("Unable to read data", e);
        }
    }

    private ReadHandle reader() throws LoggerException {
        try {
            return bookKeeper.newOpenLedgerOp()
                    .withLedgerId(ledgerMetadata.getLedgerId())
                    .withPassword("middle-earth".getBytes())
                    .withRecovery(false)
                    .execute()
                    .get();
        } catch (ExecutionException | InterruptedException e) {
            throw new LoggerException("Unable to create a new read handle", e);
        }
    }
}
