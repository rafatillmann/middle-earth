package org.example.bookkeeper;

import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.curator.framework.CuratorFramework;
import org.example.interfaces.Log;
import org.example.interfaces.LogFactory;

public class LedgerLogFactory implements LogFactory, AutoCloseable {

    private CuratorFramework zookkeeper;
    private final BookKeeper bookKeeper;

    public LedgerLogFactory(CuratorFramework zookkeeper, BookKeeper bookKeeper) {
        this.bookKeeper = bookKeeper;
    }

    @Override
    public Log createLog(long logId) {
        return new LedgerLog(logId, bookKeeper);
    }

    @Override
    public void close() throws Exception {
        this.bookKeeper.close();
    }
}
