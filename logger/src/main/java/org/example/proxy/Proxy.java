package org.example.proxy;

import org.example.exception.LoggerException;
import org.example.interfaces.Log;
import org.example.interfaces.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxy {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    private final LogFactory logFactory;
    private Log log;

    public Proxy(LogFactory logFactory) {
        this.logFactory = logFactory;
    }

    public void start(int port) throws IOException, LoggerException {

        this.log = logFactory.createLog(1);

        ServerSocket proxyServerSocket = new ServerSocket(port);
        System.out.println("Proxy server listening on port " + port);

        Socket serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
        BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream(), true);

        while (true) {
            Socket clientSocket = proxyServerSocket.accept();
            new Thread(() -> handleClient(clientSocket, serverIn, serverOut)).start();
        }
    }

    private void handleClient(Socket clientSocket, BufferedReader serverIn, PrintWriter serverOut) {
        try {
            BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);

            String request;
            while ((request = clientIn.readLine()) != null) {
                this.log.write(request.getBytes());
                serverOut.println(request);
                String response = serverIn.readLine();
                clientOut.println(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
