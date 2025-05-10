package org.example.bookkeeper;

import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.curator.framework.CuratorFramework;
import org.example.exception.LoggerException;
import org.example.interfaces.LogFactory;
import org.example.interfaces.Writer;

public class BookKeeperLogFactory implements LogFactory {

    private final CuratorFramework zookkeeper;
    private final BookKeeper bookKeeper;

    public BookKeeperLogFactory(CuratorFramework zookkeeper, BookKeeper bookKeeper) {
        this.zookkeeper = zookkeeper;
        this.bookKeeper = bookKeeper;
    }

    @Override
    public Writer getWriter(long logId) throws LoggerException {
        return new BookKeeperWriter(logId, bookKeeper);
    }
}
