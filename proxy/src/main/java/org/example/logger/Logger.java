package org.example.logger;

import org.apache.bookkeeper.client.BKException;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.LedgerEntry;
import org.apache.bookkeeper.client.LedgerHandle;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Logger {

    private static final String ZOOKEEPER_SERVER = "127.0.0.1:2181";

    private static LedgerHandle lh;

    static {
        try {
            BookKeeper bkClient = new BookKeeper(ZOOKEEPER_SERVER);
            byte[] password = "some-password".getBytes();
            lh = bkClient.createLedger(3, 2, 2, BookKeeper.DigestType.MAC, password);
        } catch (BKException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addMessage(String message) throws BKException, InterruptedException {
        lh.addEntry(message.getBytes());
    }

    public String readLastMessage() throws BKException, InterruptedException {
        LedgerEntry result = lh.readLastEntry();
        return new String(result.getEntry(), UTF_8);
    }
}
