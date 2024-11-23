package org.example.logger;

import org.apache.distributedlog.AppendOnlyStreamWriter;
import org.apache.distributedlog.DLSN;
import org.apache.distributedlog.DistributedLogConfiguration;
import org.apache.distributedlog.api.DistributedLogManager;
import org.apache.distributedlog.api.namespace.Namespace;
import org.apache.distributedlog.api.namespace.NamespaceBuilder;
import org.apache.distributedlog.exceptions.ZKException;
import org.apache.distributedlog.impl.metadata.BKDLConfig;
import org.apache.distributedlog.metadata.DLMetadata;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.apache.zookeeper.KeeperException.Code.NODEEXISTS;

public class Logger {

    private static final String ZOOKEEPER_SERVER = "127.0.0.1:2181";
    private static final String LEDGER_PAH = "/ledgers";
    private static final String LOG_NAME = "log";

    private static Namespace namespace;

    static {
        try {
            var dlogUri = initializeNamespace();

            DistributedLogConfiguration dlogConf = new DistributedLogConfiguration()
                    .setEnsembleSize(3)
                    .setWriteQuorumSize(2)
                    .setAckQuorumSize(2);

            namespace = NamespaceBuilder.newBuilder()
                    .conf(dlogConf)
                    .clientId("clientId")
                    .uri(dlogUri)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DLSN writeLog(String record) throws IOException, ExecutionException, InterruptedException {
        DistributedLogManager dlm = namespace.openLog(LOG_NAME);
        AppendOnlyStreamWriter writer = dlm.getAppendOnlyStreamWriter();
        CompletableFuture<DLSN> result = writer.write(record.getBytes());
        writer.close();
        dlm.close();
        return result.get();
    }

    public String readLog(DLSN dlsn) throws IOException {
        DistributedLogManager dlm = namespace.openLog(LOG_NAME);
        var reader = dlm.openLogReader(dlsn);
        var result = new String(reader.readNext(false).getPayload());
        reader.close();
        dlm.close();
        return result;
    }

    private static URI initializeNamespace() throws IOException {
        BKDLConfig bkdlConfig = new BKDLConfig(ZOOKEEPER_SERVER, LEDGER_PAH);
        DLMetadata dlMetadata = DLMetadata.create(bkdlConfig);

        var dlogUri = URI.create(String.format("distributedlog://%s/distributedlog", ZOOKEEPER_SERVER));

        try {
            dlMetadata.create(dlogUri);
        } catch (ZKException e) {
            if (e.getKeeperExceptionCode() == NODEEXISTS) {
                return dlogUri;
            }
            throw e;
        }
        return dlogUri;
    }

}
