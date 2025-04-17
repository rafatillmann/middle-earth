package org.example;

import org.apache.bookkeeper.client.DefaultEnsemblePlacementPolicy;
import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.example.bookkeeper.BookKeeperLogManagerFactory;
import org.example.config.Config;
import org.example.interfaces.LogManagerFactory;
import org.example.proxy.Proxy;

public class Starter {

    private CuratorFramework zookkeeper;
    private BookKeeper bookKeeper;
    private LogManagerFactory logManagerFactory;
    private Proxy proxy;

    public static void main(String[] args) {
        var starter = new Starter();
        try {
            starter.start(Config.getServerPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start(int port) throws Exception {
        zookkeeper = getZkClient();
        bookKeeper = getBkClient();
        logManagerFactory = getLogFactory();
        proxy = getProxy();
        proxy.start(port);
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

    private LogManagerFactory getLogFactory() {
        return new BookKeeperLogManagerFactory(zookkeeper, bookKeeper);
    }

    private Proxy getProxy() {
        return new Proxy(logManagerFactory);
    }
}
