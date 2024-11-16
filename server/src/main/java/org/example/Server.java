package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int SERVER_PORT = 6000;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("Server listening on port " + SERVER_PORT);
        Socket socket = serverSocket.accept();

        try (BufferedReader clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter clientOut = new PrintWriter(socket.getOutputStream(), true)) {

            String request;
            while ((request = clientIn.readLine()) != null) {
                System.out.println("Received from client: " + request);
                String response = "Echo: " + request;
                clientOut.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
