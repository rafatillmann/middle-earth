package org.example.interfaces;

public interface Log {

    public long write(byte[] data) throws Exception;

    public byte[] read(long id) throws Exception;
}
