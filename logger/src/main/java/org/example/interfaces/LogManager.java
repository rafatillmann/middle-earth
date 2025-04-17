package org.example.interfaces;

import org.example.exception.LoggerException;

import java.net.Socket;
import java.util.List;

public interface LogManager {

    long write(byte[] data) throws LoggerException;

    long write(byte[] data, Socket socketToReply) throws LoggerException;

    Entry read(long entryId) throws LoggerException;

    List<Entry> read(long firstEntryId, long lastEntryId) throws LoggerException;

    void addClientToReply(long entryId, Socket socket);

    Socket getClientToReply(long entryId);

}
