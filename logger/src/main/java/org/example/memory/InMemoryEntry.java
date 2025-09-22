package org.example.memory;

import org.example.interfaces.Entry;

public record InMemoryEntry(long entryId, byte[] payload) implements Entry {
}
