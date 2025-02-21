package org.example;

import org.apache.bookkeeper.client.api.BookKeeper;
import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.example.bookkeeper.LedgerLogFactory;
import org.example.interfaces.LogFactory;

public class Starter {

    private CuratorFramework zookkeeper;
    private BookKeeper bookKeeper;
    private LogFactory logFactory;


    public static void main(String[] args) {
        var starter = new Starter();
        try {
            starter.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start() throws Exception {
        zookkeeper = getZkClient();
        bookKeeper = getBkClient();
        logFactory = getLogFactory();
    }

    private CuratorFramework getZkClient() {
        // TODO - Adicionar as configurações de conexão do ZooKeeper
        CuratorFramework zkClient = CuratorFrameworkFactory
                .builder()
                //.connectString(this.serviceConfig.getZkURL())
                .connectString("")
                .namespace("middle-earth")
                //.retryPolicy(new ExponentialBackoffRetry(this.serviceConfig.getZkRetrySleepMs(), this.serviceConfig.getZkRetryCount()))
                .retryPolicy(new ExponentialBackoffRetry(1, 1))
                .build();
        zkClient.start();
        return zkClient;
    }

    private BookKeeper getBkClient() throws Exception {
        // TODO - Adicionar as configurações de conexão no BookKeeper
        ClientConfiguration config = new ClientConfiguration()
                .setClientTcpNoDelay(true);
        return BookKeeper.newBuilder(config).build();

    }

    private LogFactory getLogFactory() {
        return new LedgerLogFactory(zookkeeper, bookKeeper);
    }
}
