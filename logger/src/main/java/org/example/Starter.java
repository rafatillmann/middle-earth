package org.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.bookkeeper.client.DefaultEnsemblePlacementPolicy;
import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.example.ambassador.Ambassador;
import org.example.bookkeeper.BookKeeperLoggerFactory;
import org.example.config.Config;
import org.example.interfaces.LoggerFactory;

@Slf4j
public class Starter {

    private CuratorFramework zookkeeper;
    private BookKeeper bookKeeper;
    private LoggerFactory loggerFactory;
    private Ambassador ambassador;

    public static void main(String[] args) {
        var starter = new Starter();
        try {
            starter.start();
        } catch (Exception e) {
            log.debug("Unable to start logger", e);
        }
    }

    private void start() throws Exception {
        zookkeeper = getZkClient();
        bookKeeper = getBkClient();
        loggerFactory = getLogFactory();
        ambassador = getProxy();
        ambassador.initialize();
    }

    private CuratorFramework getZkClient() {
        CuratorFramework zkClient = CuratorFrameworkFactory
                .builder()
                .connectString(Config.getZkUri())
                .namespace("middle-earth")
                .retryPolicy(new ExponentialBackoffRetry(Config.getZkRetrySleepMs(), Config.getZkRetryCount()))
                .build();
        zkClient.start();
        return zkClient;
    }

    private BookKeeper getBkClient() throws Exception {
        ClientConfiguration config = new ClientConfiguration()
                .setClientTcpNoDelay(true)
                .setMetadataServiceUri("zk://localhost:2181/ledgers")
                .setEnsemblePlacementPolicy(DefaultEnsemblePlacementPolicy.class);
        return BookKeeper.newBuilder(config).build();

    }

    private LoggerFactory getLogFactory() {
        return new BookKeeperLoggerFactory(zookkeeper, bookKeeper);
    }

    private Ambassador getProxy() {
        return new Ambassador(loggerFactory);
    }
}
