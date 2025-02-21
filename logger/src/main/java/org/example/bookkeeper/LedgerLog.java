package org.example.bookkeeper;

import org.apache.bookkeeper.client.api.BKException;
import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.bookkeeper.client.api.ReadHandle;
import org.apache.bookkeeper.client.api.WriteHandle;
import org.example.exception.LoggerException;
import org.example.interfaces.Log;

public class LedgerLog implements Log {

    private final BookKeeper bookKeeper;
    private WriteHandle writer;
    private ReadHandle reader;

    public LedgerLog(long logId, BookKeeper bookKeeper) {
        this.bookKeeper = bookKeeper;
    }

    @Override
    public long write(byte[] data) throws Exception {
        try {
            return writer.append(data);
        } catch (BKException | InterruptedException e) {
            throw new LoggerException("Unable to append data", e);
        }
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
}
