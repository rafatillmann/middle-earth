package org.example;

import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.BookKeeper.DigestType;
import org.apache.bookkeeper.client.LedgerHandle;
import org.apache.distributedlog.api.namespace.Namespace;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ProxyLedger {

    private static Namespace namespace;

    public static void main(String[] args) throws IOException, BKException, InterruptedException {

        var proxyPort = 6000;
        var serverHost = "localhost";
        var serverPort = 5000;

        var zookeeperServers = "127.0.0.1:2181";
        
        BookKeeper bkClient = new BookKeeper(zookeeperServers);
        byte[] password = "some-password".getBytes();
        LedgerHandle lh = bkClient.createLedger(3, 2, 2, DigestType.MAC, password);

        ServerSocket proxyServerSocket = new ServerSocket(proxyPort);
        System.out.println("Proxy server listening on port " + proxyPort);

        while (true) {
            Socket clientSocket = proxyServerSocket.accept();
            System.out.println("Client connected");
            // Handle each connection in a new thread
            new Thread(() -> handleClient(clientSocket, serverHost, serverPort, lh)).start();
        }
    }

    private static void handleClient(Socket clientSocket, String serverHost, int serverPort, LedgerHandle lh) {
        try (Socket serverSocket = new Socket(serverHost, serverPort);
             BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
             PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream(), true)) {

            //var logName = String.format("Client%s", clientSocket.hashCode());

            // Upload data to DL
            uploadLog(clientIn, lh);
            // Forward data from client to server
            forwardClientToServe(serverOut, lh);
            // Forward data from server to client
            forwardServerToClient(serverIn, clientOut);
        } catch (IOException | BKException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void uploadLog(BufferedReader input, LedgerHandle lh) throws IOException {
        try {
            var request = input.readLine();
            lh.addEntry(request.getBytes());
        } catch (IOException | InterruptedException | BKException e) {
            e.printStackTrace();
        }
    }

    private static void forwardClientToServe(PrintWriter output, LedgerHandle lh) throws BKException, InterruptedException, UnsupportedEncodingException {
        var result = lh.readLastEntry();
        var retrEntry = new String(result.getEntry(), UTF_8);
        output.println(retrEntry);
    }

    private static void forwardServerToClient(BufferedReader input, PrintWriter output) {
        try {
            String request = input.readLine();
            output.println(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
