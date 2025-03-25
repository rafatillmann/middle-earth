package org.example.bookkeeper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.curator.framework.CuratorFramework;
import org.example.config.Config;
import org.example.cursor.LedgerCursor;
import org.example.exception.LoggerException;
import org.example.interfaces.Log;
import org.example.interfaces.LogCursor;
import org.example.interfaces.LogFactory;

public class LedgerLogFactory implements LogFactory, AutoCloseable {

	private CuratorFramework zookkeeper;
	private final BookKeeper bookKeeper;

	public LedgerLogFactory(CuratorFramework zookkeeper, BookKeeper bookKeeper) {
		this.zookkeeper = zookkeeper;
		this.bookKeeper = bookKeeper;
	}

	@Override
	public Log open(long logId) throws LoggerException {
		return new LedgerLog(logId, bookKeeper, cursors(logId));
	}

	@Override
	public Set<LogCursor> cursors(long logId) {
		Set<LogCursor> cursors = new HashSet<>();
		for (Map.Entry<String, String> entry : Config.getReplicaInfo().entrySet()) {
			cursors.add(new LedgerCursor(logId, entry.getKey(), entry.getValue()));
		}
		return cursors;
	}

	@Override
	public void close() throws Exception {
		this.bookKeeper.close();
	}
}
