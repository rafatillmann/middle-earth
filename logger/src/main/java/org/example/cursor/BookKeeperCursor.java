package org.example.cursor;

import org.example.exception.LoggerException;
import org.example.interfaces.Cursor;
import org.example.interfaces.Entry;
import org.example.interfaces.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BookKeeperCursor implements Cursor {

    private final Log log;
    private final String name;
    private final URI uri;

    // TODO - Add lastEntryId to zookeeper
    private long lastReadEntryId = -1;
    Socket serverSocket;

    public BookKeeperCursor(Log log, String name, URI uri) throws LoggerException {
        this.log = log;
        this.name = name;
        this.uri = uri;

        try {
            serverSocket = new Socket(uri.getHost(), uri.getPort());
        } catch (IOException e) {
            throw new LoggerException("Unable to open Socket", e);
        }
    }

    @Override
    public void notifyCursor(long toEntryId) throws LoggerException {
        try {
            PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream(), true);

            var fromEntryId = lastReadEntryId == -1 ? 0 : lastReadEntryId + 1;
            for (Entry entry : log.read(fromEntryId, toEntryId)) {
                serverOut.println(new String(entry.getPayload(), UTF_8));
                lastReadEntryId = entry.getEntryId();
            }
        } catch (IOException e) {
            throw new LoggerException("Unable to send entries to server", e);
        }
    }

    public void replyClient() throws LoggerException {
        try {
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        } catch (IOException e) {
            throw new LoggerException("Unable to send entries to server", e);
        }
    }
}
