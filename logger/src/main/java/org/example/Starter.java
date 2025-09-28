package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.gateway.SocketGateway;
import org.example.interfaces.Gateway;
import org.example.interfaces.LogFactory;
import org.example.memory.InMemoryLogFactory;

@Slf4j
public class Starter {
    //    private CuratorFramework zookkeeper;
    //    private BookKeeper bookKeeper;
    private LogFactory logFactory;
    private Gateway gateway;

    public static void main(String[] args) {
        var starter = new Starter();
        try {
            starter.start();
        } catch (Exception e) {
            log.debug("Unable to start logger", e);
        }
        starter.close();
    }

    private void start() throws Exception {
//        zookkeeper = getZkClient();
//        bookKeeper = getBkClient();
        logFactory = getLogFactory();
        gateway = getGateway();
        gateway.initialize();
    }

//    private CuratorFramework getZkClient() {
//        CuratorFramework zkClient = CuratorFrameworkFactory
//                .builder()
//                .connectString(Config.getZkUri())
//                .namespace("middle-earth")
//                .retryPolicy(new ExponentialBackoffRetry(Config.getZkRetrySleepMs(), Config.getZkRetryCount()))
//                .build();
//        zkClient.start();
//        return zkClient;
//    }
//
//    private BookKeeper getBkClient() throws Exception {
//        ClientConfiguration config = new ClientConfiguration()
//                .setClientTcpNoDelay(true)
//                .setMetadataServiceUri(String.format("zk://%s/ledgers", Config.getZkUri()))
//                .setEnsemblePlacementPolicy(DefaultEnsemblePlacementPolicy.class);
//        return BookKeeper.newBuilder(config).build();
//
//    }

    private LogFactory getLogFactory() {
        return new InMemoryLogFactory();
    }

    private Gateway getGateway() {
        return new SocketGateway(logFactory);
    }

    private void close() {
        try {
            gateway.close();
        } catch (Exception e) {
            log.debug("Unable to close resource", e);
        }
    }
}
