package org.example.gateway;

import lombok.Getter;
import lombok.Setter;

import java.io.PrintWriter;

@Getter
@Setter
public class SocketClient {

    private boolean replied;
    private PrintWriter out;

    public SocketClient(PrintWriter out) {
        this.out = out;
        this.replied = false;
    }
}