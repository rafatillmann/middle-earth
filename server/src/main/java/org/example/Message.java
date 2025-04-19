package org.example;

public record Message(String operation, int key, String value) {

    @Override
    public String toString() {
        return "Message{" +
                "operation='" + operation + '\'' +
                ", key=" + key +
                ", value='" + value + '\'' +
                '}';
    }
}