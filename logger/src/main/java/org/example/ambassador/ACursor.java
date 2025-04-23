package org.example.ambassador;

import org.example.exception.LoggerException;
import org.example.interfaces.Cursor;
import org.example.interfaces.Entry;
import org.example.interfaces.Reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ACursor implements Cursor {

    private final URI uri;
    private final Reader reader;
    private final Ambassador ambassador;
    //private final AtomicLong lastReadEntryId;
    private final Socket socket;
    private long lastReadEntryId;

    public ACursor(URI uri, Ambassador ambassador, Reader reader) throws LoggerException {
        this.uri = uri;
        this.reader = reader;
        this.ambassador = ambassador;
        this.lastReadEntryId = -1;
        this.socket = getSocket();
    }

    @Override
    public synchronized void entryAvailable(long toEntryId) throws LoggerException {
        try {
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // TODO - Verify if is enough AtomicLong for lastReadEntryId
            if (lastReadEntryId > toEntryId) {
                // Another thread already read this entries
                return;
            }

            var fromEntryId = ++lastReadEntryId;
            for (Entry entry : reader.read(fromEntryId, toEntryId)) {
                serverOut.println(new String(entry.getPayload(), UTF_8));
                var reply = serverIn.readLine();

                // TODO - Analyse whether Cursor needs to be responsible for this
                var clientSocket = ambassador.getClientToReply(entry.getEntryId());
                if (clientSocket != null) {
                    PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientOut.println(reply);
                }
            }
            lastReadEntryId = toEntryId;
        } catch (IOException e) {
            throw new LoggerException("Unable to send entries to server", e);
        }
    }

    public Socket getSocket() throws LoggerException {
        try {
            return new Socket(uri.getHost(), uri.getPort());
        } catch (IOException e) {
            throw new LoggerException("Unable to open Socket", e);
        }
    }

}
