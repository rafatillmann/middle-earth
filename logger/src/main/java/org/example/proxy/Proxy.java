package org.example.proxy;

import org.example.config.Config;
import org.example.exception.LoggerException;
import org.example.interfaces.Log;
import org.example.interfaces.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxy {

    private final LogFactory logFactory;
    private Log log;

    public Proxy(LogFactory logFactory) {
        this.logFactory = logFactory;
    }

    public void start(int port) throws IOException, LoggerException {
        ServerSocket proxyServerSocket = new ServerSocket(port);
        System.out.println("Proxy server listening on port " + port);

        log = logFactory.open(Config.getLogId());

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
                log.write(request.getBytes(), clientSocket);
            }
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
