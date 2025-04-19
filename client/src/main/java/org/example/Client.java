package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final String PROXY_HOST = "localhost";
    private static final int PROXY_PORT = 5000;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        try (Socket socket = new Socket(PROXY_HOST, PROXY_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            while (true) {
                int randomNumber = (int) (Math.random() * 100);
                Message message = new Message("set", randomNumber, String.valueOf(randomNumber));

                String jsonRequest = objectMapper.writeValueAsString(message);
                System.out.println("Sending JSON to server: " + jsonRequest);
                out.println(jsonRequest);

                String jsonResponse = in.readLine();
                System.out.println("Received JSON from server: " + jsonResponse);

                Thread.sleep(150);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}