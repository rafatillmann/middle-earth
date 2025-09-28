package org.example.gateway;

import lombok.extern.slf4j.Slf4j;
import org.example.config.Config;
import org.example.exception.LoggerException;
import org.example.interfaces.Cursor;
import org.example.interfaces.Gateway;
import org.example.interfaces.LogFactory;
import org.example.interfaces.Writer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class SocketGateway implements Gateway {

    private final LogFactory logFactory;
    private Writer writer;
    private Set<Cursor> cursors;
    private final AtomicInteger counter = new AtomicInteger(0);

    public SocketGateway(LogFactory logFactory) {
        this.logFactory = logFactory;
    }

    @Override
    public void initialize() throws LoggerException {
        writer = logFactory.getWriter(Config.getLogId());
        cursors = getCursors(writer);

        try (ServerSocket proxyServerSocket = new ServerSocket(Config.getServerPort());) {
            log.info("Server listening on port {}", Config.getServerPort());

            new Thread(() -> stats(1000)).start();

            while (true) {
                Socket clientSocket = proxyServerSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            throw new LoggerException("Unable to start server", e);
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String request;
            while ((request = clientIn.readLine()) != null) {
                writer.write(request.getBytes(), entryId -> callbackAddEntry(entryId, clientOut));
                counter.getAndIncrement();
            }
        } catch (Exception e) {
            log.error("Unable to process client request", e);
        }
    }

    private void callbackAddEntry(Long toEntryId, PrintWriter clientOut) {
        List<CompletableFuture<String>> replyFutures = cursors.stream()
                .map(cursor -> {
                    try {
                        return cursor.entryAvailable(toEntryId);
                    } catch (LoggerException e) {
                        log.warn("Unable to notify cursors", e);
                        return CompletableFuture.<String>failedFuture(e);
                    }
                })
                .toList();

        CompletableFuture.anyOf(replyFutures.toArray(new CompletableFuture[0]))
                .thenAccept(clientOut::println)
                .exceptionally(ex -> {
                    log.error("An error occurred during asynchronous cursor processing", ex);
                    return null;
                });

    }

    private Set<Cursor> getCursors(Writer writer) {
        return Config.getReplicaInfo().values().stream()
                .map(uri -> {
                    try {
                        return new SocketCursor(uri, writer.getReader());
                    } catch (LoggerException | IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
    }

    private void stats(int metricTime) {
        try (FileWriter writer = new FileWriter("l-throughput.csv", true)) {
            String log;
            int actualValueCounter;
            while (true) {
                Thread.sleep(metricTime);
                actualValueCounter = counter.getAndSet(0);
                log = String.format("%d,%d\n", actualValueCounter, System.nanoTime());
                writer.write(log);
                writer.flush();
            }
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        try {
            for (Cursor cursor : cursors) {
                cursor.close();
            }
        } catch (Exception e) {
            log.error("Unable to close resource", e);
        }
    }
}
