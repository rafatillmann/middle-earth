package org.example.bookkeeper;

import lombok.Getter;
import org.example.interfaces.Entry;

public class BookKeeperEntry implements Entry {

    @Getter
    private final long ledgerId;
    private final long entryId;
    private final byte[] payload;

    public BookKeeperEntry(long ledgerId, long entryId, byte[] payload) {
        this.ledgerId = ledgerId;
        this.entryId = entryId;
        this.payload = payload;
    }

    @Override
    public long getEntryId() {
        return entryId;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }
}
