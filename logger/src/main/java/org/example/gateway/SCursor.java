package org.example.gateway;

import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class SCursor implements Cursor {

    private final URI uri;
    private final Reader reader;
    private final SGateway gateway;
    private final AtomicLong lastReadEntryId;
    private final Socket socket;

    public SCursor(URI uri, SGateway SGateway, Reader reader) throws LoggerException {
        this.uri = uri;
        this.reader = reader;
        this.gateway = SGateway;
        this.lastReadEntryId = new AtomicLong(-1);
        this.socket = getSocket();
    }

    @Override
    public synchronized void entryAvailable(long toEntryId) throws LoggerException {
        try {
            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
            if (lastReadEntryId.get() > toEntryId) {
                // Another thread already read this entries
                return;
            }
            var fromEntryId = lastReadEntryId.incrementAndGet();
            for (Entry entry : reader.read(fromEntryId, toEntryId)) {
                serverOut.println(new String(entry.getPayload(), UTF_8));
                var reply = serverIn.readLine();

                // TODO - Analyse whether Cursor needs to be responsible for this
                var clientSocket = gateway.getClientToReply(entry.getEntryId());
                if (clientSocket != null) {
                    PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientOut.println(reply);
                }
            }
            lastReadEntryId.set(toEntryId);
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
