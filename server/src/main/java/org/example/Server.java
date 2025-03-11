package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Server {
    private static final Map<Integer, String> store = new HashMap<>();

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
        try (BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request;
            while ((request = clientIn.readLine()) != null) {
                System.out.println("Received from client: " + request);
                String response = process(request);
                clientOut.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String process(String request) {
        Pattern pattern = Pattern.compile("(get|set|delete)\\((\\d+)(?:,\\s*(.*))?\\)");
        Matcher matcher = pattern.matcher(request);

        if (matcher.matches()) {
            String operation = matcher.group(1);
            int key = Integer.parseInt(matcher.group(2));
            String value = matcher.group(3);

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
        } else {
            return "Invalid request";
        }
    }
}
