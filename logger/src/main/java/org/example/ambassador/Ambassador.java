package org.example.ambassador;

import lombok.extern.slf4j.Slf4j;
import org.example.config.Config;
import org.example.exception.LoggerException;
import org.example.interfaces.Cursor;
import org.example.interfaces.Logger;
import org.example.interfaces.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class Ambassador {

    private final LoggerFactory loggerFactory;
    private Logger logger;
    private Set<Cursor> cursors;

    private final ConcurrentHashMap<Long, Socket> clientsToReply = new ConcurrentHashMap<>();

    public Ambassador(LoggerFactory loggerFactory) {
        this.loggerFactory = loggerFactory;
    }

    public void initialize() throws LoggerException, IOException {
        logger = loggerFactory.open(Config.getLogId());
        cursors = getCursors(logger);
        start();
    }

    public void addClientToReply(long entryId, Socket socket) {
        clientsToReply.put(entryId, socket);
    }

    public Socket getClientToReply(long entryId) {
        return clientsToReply.remove(entryId);
    }

    private void start() throws IOException {
        try (ServerSocket proxyServerSocket = new ServerSocket(Config.getServerPort());) {
            log.info("Server listening on port " + Config.getServerPort());

            while (true) {
                Socket clientSocket = proxyServerSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String request;
            while ((request = clientIn.readLine()) != null) {
                logger.write(request.getBytes(), entryId -> notifyAddEntry(entryId, clientSocket));
            }
        } catch (Exception e) {
            log.error("Unable to process client request", e);
        }
    }

    private void notifyAddEntry(Long entryId, Socket clientSocket) {
        for (Cursor cursor : cursors) {
            try {
                addClientToReply(entryId, clientSocket);
                cursor.entryAvailable(entryId);
            } catch (LoggerException e) {
                log.warn("Unable to notify cursors", e);
            }
        }
    }

    private Set<Cursor> getCursors(Logger logger) {
        return Config.getReplicaInfo().values().stream()
                .map(uri -> {
                    try {
                        return new ACursor(uri, this, logger.getReader());
                    } catch (LoggerException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
    }
}
