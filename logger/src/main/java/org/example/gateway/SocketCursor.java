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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class SocketCursor implements Cursor {

    private final URI uri;
    private final Reader reader;
    private final AtomicLong lastReadEntryId;
    private final Socket socket;
    private final BufferedReader serverIn;
    private final PrintWriter serverOut;
    private final HashMap<Long, String> pendingReply;

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public SocketCursor(URI uri, Reader reader) throws LoggerException, IOException {
        this.uri = uri;
        this.reader = reader;
        this.lastReadEntryId = new AtomicLong(-1);
        this.socket = getSocket();
        this.serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.serverOut = new PrintWriter(socket.getOutputStream(), true);
        this.pendingReply = new HashMap<>();
    }

    @Override
    public CompletableFuture<String> entryAvailable(long toEntryId) throws LoggerException {
//        return CompletableFuture.supplyAsync(() -> {
//            try {
//                synchronized (this) {
//                    if (lastReadEntryId.get() > toEntryId) {
//                        // Another thread already read this entry or entries
//                        return pendingReply.remove(toEntryId);
//                    }
//
//                    var fromEntryId = lastReadEntryId.incrementAndGet();
//                    for (Entry entry : reader.read(fromEntryId, toEntryId)) {
//                        byte[] payload = entry.payload();
//                        serverOut.println(new String(payload, StandardCharsets.UTF_8));
//                        serverOut.flush();
//
//                        String reply = serverIn.readLine();
//                        pendingReply.put(entry.entryId(), reply);
//                    }
//
//                    lastReadEntryId.set(toEntryId);
//                    return pendingReply.remove(toEntryId);
//                }
//            } catch (Exception e) {
//                throw new RuntimeException("Unable to send entries to server", e);
//            }
//        }, executor);

        CompletableFuture<String> replyFuture = new CompletableFuture<>();
        executor.submit(() -> {
            try {
                synchronized (this) {
                    if (lastReadEntryId.get() > toEntryId) {
                        // Another thread already read this entry or entries
                        replyFuture.complete(pendingReply.remove(toEntryId));
                        return;
                    }

                    var fromEntryId = lastReadEntryId.incrementAndGet();
                    for (Entry entry : reader.read(fromEntryId, toEntryId)) {
                        byte[] payload = entry.payload();
                        serverOut.println(new String(payload, StandardCharsets.UTF_8));
                        serverOut.flush();

                        String reply = serverIn.readLine();
                        pendingReply.put(entry.entryId(), reply);
                    }

                    lastReadEntryId.set(toEntryId);
                    replyFuture.complete(pendingReply.remove(toEntryId));
                }
            } catch (Exception e) {
                replyFuture.completeExceptionally(new LoggerException("Unable to send entries to server", e));
            }
        });

        return replyFuture;
    }

    public Socket getSocket() throws LoggerException {
        try {
            return new Socket(uri.getHost(), uri.getPort());
        } catch (IOException e) {
            throw new LoggerException("Unable to open Socket", e);
        }
    }

    @Override
    public void close() {
        try {
            executor.shutdown();
            socket.close();
            serverIn.close();
            serverOut.close();
        } catch (IOException e) {
            log.error("Unable to close socket", e);
        }
    }
}
