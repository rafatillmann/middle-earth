package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static java.lang.System.in;

public class ClientMessage {
    public static void main(String[] args) {
        String proxyHost = "localhost"; // Proxy server hostname
        int proxyPort = 6000; // Proxy server port
        BufferedReader input = new BufferedReader(new InputStreamReader(in));
        String line;
        try (Socket socket = new Socket(proxyHost, proxyPort);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            while (!(line = input.readLine()).equals("Over")) {
                System.out.println("Sending to server: " + line);
                out.println(line);
                String response = in.readLine();
                System.out.println("Received from server: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
