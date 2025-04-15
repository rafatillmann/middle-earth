package org.example.bookkeeper;

import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.curator.framework.CuratorFramework;
import org.example.config.Config;
import org.example.cursor.BookKeeperCursor;
import org.example.exception.LoggerException;
import org.example.interfaces.Cursor;
import org.example.interfaces.Log;
import org.example.interfaces.LogFactory;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BookKeeperLogFactory implements LogFactory {

    private final CuratorFramework zookkeeper;
    private final BookKeeper bookKeeper;

    public BookKeeperLogFactory(CuratorFramework zookkeeper, BookKeeper bookKeeper) {
        this.zookkeeper = zookkeeper;
        this.bookKeeper = bookKeeper;
    }

    @Override
    public Log open(long logId) throws LoggerException {
        var bookkeeperLog = new BookKeeperLog(logId, bookKeeper);
        bookkeeperLog.initializeCursors(getCursors(bookkeeperLog));
        return bookkeeperLog;
    }

    private Set<Cursor> getCursors(Log log) throws LoggerException {
        Set<Cursor> cursors = new HashSet<>();
        for (Map.Entry<String, URI> entry : Config.getReplicaInfo().entrySet()) {
            cursors.add(new BookKeeperCursor(log, entry.getKey(), entry.getValue()));
        }
        return cursors;
    }
}
