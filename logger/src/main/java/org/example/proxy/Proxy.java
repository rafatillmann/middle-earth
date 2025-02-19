package org.example.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxy {

    private static final int PROXY_PORT = 6000;
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) throws IOException {

        ServerSocket proxyServerSocket = new ServerSocket(PROXY_PORT);
        System.out.println("Proxy server listening on port " + PROXY_PORT);

        Socket serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
        BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream(), true);

        while (true) {
            Socket clientSocket = proxyServerSocket.accept();
            new Thread(() -> handleClient(clientSocket, serverIn, serverOut)).start();
        }
    }

    private static void handleClient(Socket clientSocket, BufferedReader serverIn, PrintWriter serverOut) {
        try {
            BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);

            String message;
            while ((message = clientIn.readLine()) != null) {

                serverOut.println(message);

                String request = serverIn.readLine();
                clientOut.println(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
