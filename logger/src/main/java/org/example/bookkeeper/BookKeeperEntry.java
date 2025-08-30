package org.example.bookkeeper;

import lombok.Getter;
import org.example.interfaces.Entry;

public record BookKeeperEntry(@Getter long ledgerId, long entryId, byte[] payload) implements Entry {

}
