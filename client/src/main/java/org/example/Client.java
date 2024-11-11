package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Client {

    private static final String PROXY_HOST = "localhost";
    private static final int PROXY_PORT = 6000;
    private static final int NUM_CONNECTIONS = 20;


    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < NUM_CONNECTIONS; i++) {
            new Thread(Client::newConnection).start();
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    private static void newConnection() {
        try (Socket socket = new Socket(PROXY_HOST, PROXY_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Sending to server: Test");
            out.println("Test");

            String response = in.readLine();
            System.out.println("Received from server: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
