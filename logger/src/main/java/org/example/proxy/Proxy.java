package org.example.proxy;

import org.example.config.Config;
import org.example.exception.LoggerException;
import org.example.interfaces.LogManager;
import org.example.interfaces.LogManagerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxy {

    private final LogManagerFactory logManagerFactory;
    private LogManager logManager;

    public Proxy(LogManagerFactory logManagerFactory) {
        this.logManagerFactory = logManagerFactory;
    }

    public void start(int port) throws IOException, LoggerException {
        ServerSocket proxyServerSocket = new ServerSocket(port);
        System.out.println("Proxy server listening on port " + port);

        logManager = logManagerFactory.open(Config.getLogId());

        while (true) {
            Socket clientSocket = proxyServerSocket.accept();
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String request;
            while ((request = clientIn.readLine()) != null) {
                logManager.write(request.getBytes(), clientSocket);
            }
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
