package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final String PROXY_HOST = "localhost";
    private static final int PROXY_PORT = 6000;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        int numberOfThreads = Integer.parseInt(args[0]);
        int numberOfRequests = Integer.parseInt(args[1]);
        String op = args[2];
        createThreads(numberOfThreads, numberOfRequests, op);
    }

    private static void createThreads(int numberOfThreads, int numberOfRequests, String op) {
        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(() -> runClientRequest(numberOfRequests, op)).start();
        }
    }

    private static void runClientRequest(int numberOfRequests, String op) {
        try (Socket socket = new Socket(PROXY_HOST, PROXY_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            for (int j = 0; j < numberOfRequests; j++) {
                int randomNumber = (int) (Math.random() * 100);
                Message message = new Message(op, randomNumber, String.valueOf(randomNumber));

                String jsonRequest = objectMapper.writeValueAsString(message);
                System.out.println("Thread " + Thread.currentThread().getId() + " sending JSON to server: " + jsonRequest);
                out.println(jsonRequest);

                String jsonResponse = in.readLine();
                System.out.println("Thread " + Thread.currentThread().getId() + " received JSON from server: " + jsonResponse);

                Thread.sleep(150);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
