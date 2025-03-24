package org.example.interfaces;

public interface Log {

	void write(byte[] data) throws Exception;

	byte[] read(long id) throws Exception;
}
