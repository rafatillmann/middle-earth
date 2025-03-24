package org.example.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.example.config.Config;
import org.example.exception.LoggerException;
import org.example.interfaces.Log;
import org.example.interfaces.LogFactory;

public class Proxy {

	private final LogFactory logFactory;
	private Log log;

	public Proxy(LogFactory logFactory) {
		this.logFactory = logFactory;
	}

	public void start(int port) throws IOException, LoggerException {

		this.log = logFactory.open(Config.getLogId());

		ServerSocket proxyServerSocket = new ServerSocket(port);
		System.out.println("Proxy server listening on port " + port);

		while (true) {
			Socket clientSocket = proxyServerSocket.accept();
			new Thread(() -> handleClient(clientSocket)).start();
		}
	}

	private void handleClient(Socket clientSocket) {
		try {
			BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);

			String request;
			while ((request = clientIn.readLine()) != null) {
				this.log.write(request.getBytes());
				clientOut.println("Ok!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
