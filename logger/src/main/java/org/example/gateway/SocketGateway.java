package org.example.gateway;

import lombok.extern.slf4j.Slf4j;
import org.example.config.Config;
import org.example.exception.LoggerException;
import org.example.interfaces.Cursor;
import org.example.interfaces.Gateway;
import org.example.interfaces.LogFactory;
import org.example.interfaces.Writer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class SocketGateway implements Gateway {

    private final LogFactory logFactory;
    private final Map<Long, Socket> clientsToReply = new ConcurrentHashMap<>();
    private Writer writer;
    private Set<Cursor> cursors;

    public SocketGateway(LogFactory logFactory) {
        this.logFactory = logFactory;
    }

    @Override
    public void initialize() throws LoggerException {
        writer = logFactory.getWriter(Config.getLogId());
        cursors = getCursors(writer);

        try (ServerSocket proxyServerSocket = new ServerSocket(Config.getServerPort());) {
            log.info("Server listening on port " + Config.getServerPort());

            while (true) {
                Socket clientSocket = proxyServerSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            throw new LoggerException("Unable to start server", e);
        }
    }

    public Socket getClientToReply(long entryId) {
        return clientsToReply.remove(entryId);
    }

    public void replyClient(long entryId, String reply) throws IOException {
        var clientSocket = getClientToReply(entryId);

        if (clientSocket != null) {
            PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            clientOut.println(reply);
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String request;
            while ((request = clientIn.readLine()) != null) {
                writer.write(request.getBytes(), entryId -> callbackAddEntry(entryId, clientSocket));
            }
        } catch (Exception e) {
            log.error("Unable to process client request", e);
        }
    }

    private void callbackAddEntry(Long entryId, Socket clientSocket) {
        clientsToReply.put(entryId, clientSocket);
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
                    } catch (LoggerException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
    }
}
