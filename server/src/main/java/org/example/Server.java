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
        int takeTime = Integer.parseInt(args[1]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);
            // Metrics
            new Thread(() -> stats(takeTime)).start();

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

            String request;
            while ((request = serverIn.readLine()) != null) {
                System.out.println("Received JSON from client: " + request);

                var message = objectMapper.readValue(request, Message.class);
                var actualValueCounter = counter.getAndIncrement();
                var response = processClientRequest(message);

                var jsonResponse = objectMapper.writeValueAsString(response);
                serverOut.println(jsonResponse);
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

    private static void stats(int takeTime) {
        try (FileWriter writer = new FileWriter("throughput.txt", true)) {
            while (true) {
                Thread.sleep(takeTime);
                var actualValueCounter = counter.getAndSet(0);
                var log = String.format("Throughput (/s): %d, Time: %d \n", actualValueCounter, System.nanoTime());
                writer.write(log);
                writer.flush();
            }
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
    }
}
