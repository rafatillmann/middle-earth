package org.example.bookkeeper;

import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.curator.framework.CuratorFramework;
import org.example.config.Config;
import org.example.exception.LoggerException;
import org.example.interfaces.Cursor;
import org.example.interfaces.LogManager;
import org.example.interfaces.LogManagerFactory;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BookKeeperLogManagerFactory implements LogManagerFactory {

    private final CuratorFramework zookkeeper;
    private final BookKeeper bookKeeper;

    public BookKeeperLogManagerFactory(CuratorFramework zookkeeper, BookKeeper bookKeeper) {
        this.zookkeeper = zookkeeper;
        this.bookKeeper = bookKeeper;
    }

    @Override
    public LogManager open(long logId) throws LoggerException {
        var bookkeeperLog = new BookKeeperLogManager(logId, bookKeeper);
        bookkeeperLog.initializeCursors(getCursors(bookkeeperLog));
        return bookkeeperLog;
    }

    private Set<Cursor> getCursors(LogManager logManager) {
        Set<Cursor> cursors = new HashSet<>();
        for (Map.Entry<String, URI> entry : Config.getReplicaInfo().entrySet()) {
            cursors.add(new BookKeeperCursor(logManager, entry.getKey(), entry.getValue()));
        }
        return cursors;
    }
}
