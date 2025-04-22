package org.example.bookkeeper;

import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.curator.framework.CuratorFramework;
import org.example.exception.LoggerException;
import org.example.interfaces.Logger;
import org.example.interfaces.LoggerFactory;

public class BookKeeperLoggerFactory implements LoggerFactory {

    private final CuratorFramework zookkeeper;
    private final BookKeeper bookKeeper;

    public BookKeeperLoggerFactory(CuratorFramework zookkeeper, BookKeeper bookKeeper) {
        this.zookkeeper = zookkeeper;
        this.bookKeeper = bookKeeper;
    }

    @Override
    public Logger open(long logId) throws LoggerException {
        return new BookKeeperLogger(logId, bookKeeper);
    }
}
