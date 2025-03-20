package org.example.interfaces;

import java.io.IOException;

import org.example.exception.LoggerException;

public interface Ambassador {
	void start(int port) throws IOException, LoggerException;
}
