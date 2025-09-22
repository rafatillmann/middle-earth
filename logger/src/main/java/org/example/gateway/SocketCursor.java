package org.example.gateway;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.LoggerException;
import org.example.interfaces.Cursor;
import org.example.interfaces.Entry;
import org.example.interfaces.LogCallback;
import org.example.interfaces.Reader;
import org.example.util.CacheTTL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class SocketCursor implements Cursor {

    private final URI uri;
    private final Reader reader;
    private final AtomicLong lastReadEntryId;
    private final Socket socket;
    private final BufferedReader serverIn;
    private final PrintWriter serverOut;
    // TODO - Search how use Future instead of Cache
    private final CacheTTL<Long, String> cache;

    public SocketCursor(URI uri, Reader reader) throws LoggerException, IOException {
        this.uri = uri;
        this.reader = reader;
        this.lastReadEntryId = new AtomicLong(-1);
        this.socket = getSocket();
        this.serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.serverOut = new PrintWriter(socket.getOutputStream(), true);
        this.cache = new CacheTTL<>(30, TimeUnit.SECONDS);
    }

    @Override
    public synchronized void entryAvailable(long toEntryId, LogCallback.ReplyCallback callback) throws LoggerException {
        try {
            if (lastReadEntryId.get() > toEntryId) {
                // Another thread already read this entries
                callback.onComplete(cache.getIfPresent(toEntryId));
                return;
            }
            var fromEntryId = lastReadEntryId.incrementAndGet();
            for (Entry entry : reader.read(fromEntryId, toEntryId)) {
                serverOut.println(new String(entry.payload(), UTF_8)); // Possível, por questões de rede, a réplica receber em outra ordem?
                cache.put(entry.entryId(), serverIn.readLine());
            }
            callback.onComplete(cache.getIfPresent(toEntryId));
            lastReadEntryId.set(toEntryId);
        } catch (Exception e) {
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
