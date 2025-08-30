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
        int testTime = Integer.parseInt(args[6]);
        int valueSize = Integer.parseInt(args[6]);

        createThreads(numberOfClients, numberOfRequests, thinkTime, percentRead, testTime, valueSize);
    }

    private static void createThreads(int numberOfThreads, int numberOfRequests, int thinkTime, int percentRead, int testTime, int valueSize) {
        try {
            writer = new FileWriter(String.format("%d-latency.csv", numberOfThreads), true);
        } catch (IOException e) {
            Thread.currentThread().interrupt();
        }

        new Thread(() -> runClientRequest(true, numberOfRequests, thinkTime, percentRead, valueSize)).start();
        for (int i = 0; i < numberOfThreads - 1; i++) {
            new Thread(() -> runClientRequest(false, numberOfRequests, thinkTime, percentRead, valueSize)).start();
        }

        new Thread(() -> {
            try {
                Thread.sleep(testTime);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            running = false;
        }).start();
    }

    private static void runClientRequest(boolean threadWillMeasureLatency, int numberOfRequests, int thinkTime, int percentRead, int valueSize) {
        try (Socket socket = new Socket(proxyHost, proxyPort);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            boolean readOperation;
            String op, value, jsonRequest;
            int key;
            Message message;
            long start, end;
            boolean measureCurrentRequestLatency;
            for (int j = 0; running && j < numberOfRequests; j++) {
                readOperation = rand.nextInt(100) < percentRead;
                op = readOperation ? "get" : "set";
                key = rand.nextInt(100000);
                value = readOperation ? null : "S".repeat(valueSize); // Parametrizar (4, 1024, 4096)

                message = new Message(op, key, value);
                jsonRequest = objectMapper.writeValueAsString(message);

                if (threadWillMeasureLatency) {
                    measureCurrentRequestLatency = rand.nextInt(numberOfRequests) == 0;
                    if (measureCurrentRequestLatency) {
                        start = System.nanoTime();
                        out.println(jsonRequest);
                        in.readLine();
                        end = System.nanoTime();
                        latency(op, start, end);
                    }
                } else {
                    out.println(jsonRequest);
                    in.readLine();
                }
                Thread.sleep(thinkTime);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void latency(String operation, long start, long end) {
        try {
            var log = String.format("%s,%d,%d,%d\n",
                    operation, start, end, (end - start));
            writer.append(log);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
