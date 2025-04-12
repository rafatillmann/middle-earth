package org.example.interfaces;

public interface Entry {

    long getEntryId();

    // TODO - Payload need to be an object with id and request
    byte[] getPayload();
}
