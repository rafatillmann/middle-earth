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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class SocketGateway implements Gateway {

    private final LogFactory logFactory;
    private final Map<Long, PrintWriter> clientsToReply = new ConcurrentHashMap<>();
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

    public PrintWriter getClientToReply(long entryId) {
        return clientsToReply.remove(entryId);
    }

    public void replyToClient(long entryId, String reply) throws IOException {
        var clientOut = getClientToReply(entryId);
        if (clientOut == null) {
            return;
        }
        clientOut.println(reply);
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

    private void callbackAddEntry(Long entryId, PrintWriter clientOut) {
        clientsToReply.put(entryId, clientOut);
        for (Cursor cursor : cursors) {
            try {
                cursor.entryAvailable(entryId);
            } catch (LoggerException e) {
                log.warn("Unable to notify cursors", e);
            }
        }
    }

    private Set<Cursor> getCursors(Writer writer) {
        return Config.getReplicaInfo().values().stream()
                .map(uri -> {
                    try {
                        return new SocketCursor(uri, this, writer.getReader());
                    } catch (LoggerException | IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
    }

    private void stats(int metricTime) {
        try (FileWriter writer = new FileWriter("l-throughput.txt", true)) {
            String log;
            int actualValueCounter;
            while (true) {
                Thread.sleep(metricTime);
                actualValueCounter = counter.getAndSet(0);
                log = String.format("%d,%d\n", actualValueCounter, System.nanoTime());
                writer.write(log);
                writer.flush();
                System.out.println(clientsToReply);
            }
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
    }
}
