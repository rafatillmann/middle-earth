package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class Server {
    private static final Map<Integer, String> store = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String jsonRequest;
            while ((jsonRequest = in.readLine()) != null) {
                System.out.println("Received JSON from client: " + jsonRequest);

                Message message = objectMapper.readValue(jsonRequest, Message.class);
                String response = process(message);

                String jsonResponse = objectMapper.writeValueAsString(response);
                out.println(jsonResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String process(Message message) {
        String operation = message.operation();
        int key = message.key();
        String value = message.value();

        switch (operation) {
            case "get":
                return store.getOrDefault(key, null);
            case "set":
                if (value == null) {
                    return "Invalid operation";
                }
                store.put(key, value);
                return "OK";
            case "delete":
                return store.remove(key) != null ? "OK" : "Key not found";
            default:
                return "Invalid request";
        }
    }
}
