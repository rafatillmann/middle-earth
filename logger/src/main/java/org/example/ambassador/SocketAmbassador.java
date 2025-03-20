package org.example.ambassador;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.example.exception.LoggerException;
import org.example.interfaces.Ambassador;
import org.example.interfaces.Log;
import org.example.interfaces.LogFactory;

public class SocketAmbassador implements Ambassador {

	private static final String SERVER_HOST = "localhost";
	private static final int SERVER_PORT = 5000;

	private final LogFactory logFactory;
	private Log log;

	public SocketAmbassador(LogFactory logFactory) {
		this.logFactory = logFactory;
	}

	@Override
	public void start(int port) throws IOException, LoggerException {

		this.log = logFactory.open(1);

		ServerSocket proxyServerSocket = new ServerSocket(port);
		System.out.println("Proxy server listening on port " + port);

		while (true) {
			Socket clientSocket = proxyServerSocket.accept();
			new Thread(() -> handleClient(clientSocket)).start();
		}
	}

	private void handleClient(Socket clientSocket) {
		try {
			Socket serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
			BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			PrintWriter serverOut = new PrintWriter(serverSocket.getOutputStream(), true);

			BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);

			String request;
			while ((request = clientIn.readLine()) != null) {
				this.log.write(request.getBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
