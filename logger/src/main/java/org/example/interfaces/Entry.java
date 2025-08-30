package org.example.interfaces;

public interface Entry {

    long entryId();

    // TODO - Payload need to be an object with id and request
    byte[] payload();
}
