package org.example;

import static java.lang.System.in;

import java.io.*;
import java.net.*;

public class Client {
	public static void main(String[] args) throws IOException {
		String proxyHost = "localhost"; // Proxy server hostname
		int proxyPort = 6000; // Proxy server port

		BufferedReader input = new BufferedReader(new InputStreamReader(in));
		String line;

		while (!(line = input.readLine()).equals("Over")){

			try (Socket socket = new Socket(proxyHost, proxyPort);
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

				System.out.println("Sending to server: " + line);
				out.println(line);

				String response = in.readLine();
				System.out.println("Received from server: " + response);
			}
		}
	}
}
