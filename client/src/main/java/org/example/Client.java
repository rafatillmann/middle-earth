package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class Client {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Random rand = new Random();

    private static volatile boolean running = true;

    private static FileWriter writer;
    private static String proxyHost;
    private static int proxyPort;

    public static void main(String[] args) {
        proxyHost = args[0];
        proxyPort = Integer.parseInt(args[1]);
        int numberOfClients = Integer.parseInt(args[2]);
        int numberOfRequests = Integer.parseInt(args[3]);
        int thinkTime = Integer.parseInt(args[4]);
        int percentRead = Integer.parseInt(args[5]);

        new Thread(() -> {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException ignored) {
            }
            running = false;
        }).start();

        createThreads(numberOfClients, numberOfRequests, thinkTime, percentRead);
    }

    private static void createThreads(int numberOfThreads, int numberOfRequests, int thinkTime, int percentRead) {
        try {
            writer = new FileWriter(String.format("%d-latency.txt", numberOfThreads), true);
        } catch (IOException e) {
            Thread.currentThread().interrupt();
        }
        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(() -> runClientRequest(numberOfThreads, numberOfRequests, thinkTime, percentRead)).start();
        }
    }

    private static void runClientRequest(int numberOfThreads, int numberOfRequests, int thinkTime, int percentRead) {
        try (Socket socket = new Socket(proxyHost, proxyPort);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            for (int j = 0; running && j < numberOfRequests; j++) {

                boolean readOperation = rand.nextInt(100) < percentRead;
                var op = readOperation ? "get" : "set";
                var key = rand.nextInt(100000);
                var value = readOperation ? null : "&".repeat(16);

                Message message = new Message(op, key, value);

                long start = 0;
                long end = 0;
                var measureCurrentRequestLatency = rand.nextInt(numberOfThreads) == 0;

                String jsonRequest = objectMapper.writeValueAsString(message);
                System.out.println("Thread " + Thread.currentThread().getId() + " sending JSON to server: " + jsonRequest);

                if (measureCurrentRequestLatency) {
                    start = System.nanoTime();
                }

                out.println(jsonRequest);

                String jsonResponse = in.readLine();

                if (measureCurrentRequestLatency) {
                    end = System.nanoTime();
                    latency(op, start, end);
                }

                System.out.println("Thread " + Thread.currentThread().getId() + " received JSON from server: " + jsonResponse);

                Thread.sleep(thinkTime);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static synchronized void latency(String operation, long start, long end) {
        try {
            var log = String.format("%s operation: start %d, end %d, latency %d\n",
                    operation, start, end, (end - start));
            writer.append(log);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
