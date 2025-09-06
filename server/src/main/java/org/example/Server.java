package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class Server {
    private static final Map<Integer, String> store = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        int metricTime = Integer.parseInt(args[1]);
        int numberOfClients = Integer.parseInt(args[2]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);
            // Metrics
            new Thread(() -> stats(metricTime, numberOfClients)).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader serverIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter serverOut = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request, response, jsonResponse;
            Message message;
            while ((request = serverIn.readLine()) != null) {
                message = objectMapper.readValue(request, Message.class);
                response = processClientRequest(message);
                jsonResponse = objectMapper.writeValueAsString(response);
                serverOut.println(jsonResponse);
                counter.getAndIncrement();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String processClientRequest(Message message) {
        String operation = message.operation();
        int key = message.key();
        String value = message.value();
        switch (operation) {
            case "get":
                return String.format("Key: %s, Value: %s", key, store.getOrDefault(key, "Key not found"));
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

    private static void stats(int metricTime, int numberOfClients) {
        try (FileWriter writer = new FileWriter(String.format("%d-throughput.csv", numberOfClients), true)) {
            String log;
            int actualValueCounter;
            while (true) {
                Thread.sleep(metricTime);
                actualValueCounter = counter.getAndSet(0);
                log = String.format("%d,%d\n", actualValueCounter, System.nanoTime());
                writer.write(log);
                writer.flush();
            }
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
    }
}
