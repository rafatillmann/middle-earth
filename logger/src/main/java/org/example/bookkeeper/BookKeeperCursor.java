package org.example.bookkeeper;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.LoggerException;
import org.example.interfaces.Cursor;
import org.example.interfaces.Entry;
import org.example.interfaces.LogManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class BookKeeperCursor implements Cursor {

    private final LogManager logManager;
    private final String name;
    private final URI uri;

    // TODO - Add lastEntryId to zookeeper
    private long lastReadEntryId = -1;
    private Socket serverSocket;

    public BookKeeperCursor(LogManager logManager, String name, URI uri) throws LoggerException {
        this.logManager = logManager;
        this.name = name;
        this.uri = uri;
        this.serverSocket = socket();
    }

    @Override
    public void notifyCursor(long toEntryId) throws LoggerException {
        try {
            PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream(), true);
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            var fromEntryId = lastReadEntryId == -1 ? 0 : lastReadEntryId + 1;
            for (Entry entry : logManager.read(fromEntryId, toEntryId)) {
                serverOut.println(new String(entry.getPayload(), UTF_8));
                lastReadEntryId = entry.getEntryId();

                var reply = serverIn.readLine();

                var clientSocket = logManager.getClientToReply(lastReadEntryId);
                if (clientSocket != null) {
                    PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientOut.println(reply);
                }
            }
        } catch (IOException e) {
            throw new LoggerException("Unable to send entries to server", e);
        }
    }

    public Socket socket() throws LoggerException {
        try {
            return new Socket(uri.getHost(), uri.getPort());
        } catch (IOException e) {
            throw new LoggerException("Unable to open Socket", e);
        }
    }
}
