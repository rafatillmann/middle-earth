package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        int port = 5000; // Server port

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server listening on port " + port);
        Socket socket = serverSocket.accept();
		
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            while (true) {
                String request = in.readLine();
                System.out.println("Received from client: " + request);
                String response = "Echo: " + request;
                out.println(response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
