package org.example;

import java.io.*;
import java.net.*;

public class Server {
	public static void main(String[] args) throws IOException {
		int port = 5000; // Server port

		ServerSocket serverSocket = new ServerSocket(port);
		System.out.println("Server listening on port " + port);

		while (true) {
			Socket socket = serverSocket.accept();
			System.out.println("Client connected to server");

			try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

				String request = in.readLine();
				System.out.println("Received from client: " + request);
				String response = "Echo: " + request;
				out.println(response);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
