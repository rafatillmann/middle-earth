package org.example;

import org.apache.distributedlog.DLSN;
import org.apache.distributedlog.DistributedLogConfiguration;
import org.apache.distributedlog.LogRecord;
import org.apache.distributedlog.api.DistributedLogManager;
import org.apache.distributedlog.api.LogReader;
import org.apache.distributedlog.api.LogWriter;
import org.apache.distributedlog.api.namespace.Namespace;
import org.apache.distributedlog.api.namespace.NamespaceBuilder;
import org.apache.distributedlog.exceptions.ZKException;
import org.apache.distributedlog.impl.metadata.BKDLConfig;
import org.apache.distributedlog.metadata.DLMetadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;

import static org.apache.zookeeper.KeeperException.Code.NODEEXISTS;

public class Proxy {

    private static Namespace namespace;

    public static void main(String[] args) throws IOException {

        var proxyPort = 6000;
        var serverHost = "localhost";
        var serverPort = 5000;

        var zookeeperServers = "127.0.0.1:2181";
        var ledgersPath = "/ledgers";
        var dlogUri = initializeNamespace(zookeeperServers, ledgersPath);

        DistributedLogConfiguration dlogConf = new DistributedLogConfiguration()
                .setEnsembleSize(3)
                .setWriteQuorumSize(2)
                .setAckQuorumSize(2);
        try {
            namespace = NamespaceBuilder.newBuilder()
                    .conf(dlogConf)
                    .clientId("clientId")
                    .uri(dlogUri)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ServerSocket proxyServerSocket = new ServerSocket(proxyPort);
        System.out.println("Proxy server listening on port " + proxyPort);

        while (true) {
            Socket clientSocket = proxyServerSocket.accept();
            System.out.println("Client connected");

            // Handle each connection in a new thread
            new Thread(() -> handleClient(clientSocket, serverHost, serverPort)).start();
        }
    }

    private static void handleClient(Socket clientSocket, String serverHost, int serverPort) {
        try (Socket serverSocket = new Socket(serverHost, serverPort);
             BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
             PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream(), true);) {

            String logName = String.format("Client%s", clientSocket.hashCode());

            // Upload data to DL
            uploadLog(clientIn, logName);
            // Forward data from client to server
            forwardClientToServe(serverOut, logName);
            // Forward data from server to client
            forwardServerToClient(serverIn, clientOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void uploadLog(BufferedReader input, String logName) throws IOException {
        try {
            var request = input.readLine();
            var record = new LogRecord(1L, request.getBytes());

            DistributedLogManager dlm = namespace.openLog(logName);
            LogWriter writer = dlm.openLogWriter();
            writer.write(record);
            writer.markEndOfStream();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void forwardClientToServe(PrintWriter output, String logName) {
        try {
            DistributedLogManager dlm = namespace.openLog(logName);
            LogReader reader = dlm.openLogReader(DLSN.InitialDLSN);

            var result = new String(reader.readNext(false).getPayload());
            output.println(result);
            reader.asyncClose();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void forwardServerToClient(BufferedReader input, PrintWriter output) {
        try {
            String request = input.readLine();
            output.println(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static URI initializeNamespace(String zookeeperServers, String ledgersPath) throws IOException {
        BKDLConfig bkdlConfig = new BKDLConfig(zookeeperServers, ledgersPath);
        DLMetadata dlMetadata = DLMetadata.create(bkdlConfig);

        var dlogUri = URI.create(String.format("distributedlog://%s/distributedlog", zookeeperServers));

        try {
            dlMetadata.create(dlogUri);
        } catch (ZKException e) {
            if (e.getKeeperExceptionCode() == NODEEXISTS) {
                return dlogUri;
            }
            throw e;
        }
        return dlogUri;
    }
}
