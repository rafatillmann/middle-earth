package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class Client {
    private static final String PROXY_HOST = "localhost";
    private static final int PROXY_PORT = 6000;
    private static final int bound = 100;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Random rand = new Random();

    private static FileWriter writer;

    public static void main(String[] args) {
        int numberOfClients = Integer.parseInt(args[0]);
        int numberOfRequests = Integer.parseInt(args[1]);
        int thinkTime = Integer.parseInt(args[2]);
        int percentRead = Integer.parseInt(args[3]);
        createThreads(numberOfClients, numberOfRequests, thinkTime, percentRead);
    }

    private static void createThreads(int numberOfThreads, int numberOfRequests, int thinkTime, int percentRead) {
        try {
            writer = new FileWriter("latency.txt", true);
        } catch (IOException e) {
            Thread.currentThread().interrupt();
        }

        for (int i = 0; i < numberOfThreads; i++) {
            var measureLatency = i == rand.nextInt(numberOfThreads);
            new Thread(() -> runClientRequest(numberOfRequests, measureLatency, thinkTime, percentRead)).start();
        }
    }

    private static void runClientRequest(int numberOfRequests, boolean measureLatency, int thinkTime, int percentRead) {
        try (Socket socket = new Socket(PROXY_HOST, PROXY_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            for (int j = 0; j < numberOfRequests; j++) {

                boolean readOperation = rand.nextInt(100) == percentRead;
                Message message = new Message(readOperation ? "get" : "set", (int) Thread.currentThread().getId(), readOperation ? null : String.valueOf(j));

                long start = 0;
                long end = 0;
                var measureCurrentRequestLatency = measureLatency && rand.nextInt(bound) == 0;

                String jsonRequest = objectMapper.writeValueAsString(message);
                System.out.println("Thread " + Thread.currentThread().getId() + " sending JSON to server: " + jsonRequest);

                if (measureCurrentRequestLatency) {
                    start = System.currentTimeMillis();
                }

                out.println(jsonRequest);

                String jsonResponse = in.readLine();

                if (measureCurrentRequestLatency) {
                    end = System.currentTimeMillis();
                    var log = String.format("Latency: %d, %d \n", start, end);
                    writer.append(log);
                    writer.flush();
                }

                System.out.println("Thread " + Thread.currentThread().getId() + " received JSON from server: " + jsonResponse);

                Thread.sleep(thinkTime);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
