package org.example.bookkeeper;

import java.net.URI;
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
		var ledgerLog = new LedgerLog(logId, bookKeeper);
		var cursors = initializeCursors(ledgerLog);
		ledgerLog.initialize(cursors);
		return ledgerLog;
	}

	@Override
	public Set<LogCursor> initializeCursors(Log log) {
		Set<LogCursor> cursors = new HashSet<>();
		for (Map.Entry<String, URI> entry : Config.getReplicaInfo().entrySet()) {
			cursors.add(new LedgerCursor(log, entry.getKey(), entry.getValue()));
		}
		return cursors;
	}

	@Override
	public void close() throws Exception {
		this.bookKeeper.close();
	}
}
