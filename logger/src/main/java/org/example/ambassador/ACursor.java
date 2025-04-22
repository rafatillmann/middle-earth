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

    private long lastReadEntryId = -1;
    private Socket socket;

    public ACursor(URI uri, Ambassador ambassador, Reader reader) throws LoggerException {
        this.uri = uri;
        this.reader = reader;
        this.ambassador = ambassador;
        this.socket = socket();
    }

    @Override
    public synchronized void entryAvailable(long toEntryId) throws LoggerException {
        try {
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            var fromEntryId = lastReadEntryId == -1 ? 0 : lastReadEntryId + 1;
            for (Entry entry : reader.read(fromEntryId, toEntryId)) {
                serverOut.println(new String(entry.getPayload(), UTF_8));
                lastReadEntryId = entry.getEntryId();

                var reply = serverIn.readLine();

                var clientSocket = ambassador.getClientToReply(lastReadEntryId);

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
