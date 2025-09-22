package org.example.bookkeeper;

import org.example.interfaces.Entry;

public record BookKeeperEntry(long ledgerId, long entryId, byte[] payload) implements Entry {

}
