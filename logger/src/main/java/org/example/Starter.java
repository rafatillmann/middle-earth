package org.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.bookkeeper.client.DefaultEnsemblePlacementPolicy;
import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.example.bookkeeper.BookKeeperLogFactory;
import org.example.config.Config;
import org.example.gateway.SocketGateway;
import org.example.interfaces.Gateway;
import org.example.interfaces.LogFactory;

@Slf4j
public class Starter {

    private CuratorFramework zookkeeper;
    private BookKeeper bookKeeper;
    private LogFactory logFactory;
    private Gateway gateway;

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
        logFactory = getLogFactory();
        gateway = getGateway();
        gateway.initialize();
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

    private LogFactory getLogFactory() {
        return new BookKeeperLogFactory(zookkeeper, bookKeeper);
    }

    private Gateway getGateway() {
        return new SocketGateway(logFactory);
    }
}
