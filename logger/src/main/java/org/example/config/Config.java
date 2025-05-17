package org.example.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

@Slf4j
public class Config {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Unable to find config.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load configuration properties");
        }
    }

    private static String getOverrideOrDefault(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }

    public static int getZkRetrySleepMs() {
        return Integer.parseInt(getOverrideOrDefault("zk.retry.sleep.ms", properties.getProperty("zk.retry.sleep.ms")));
    }

    public static int getZkRetryCount() {
        return Integer.parseInt(getOverrideOrDefault("zk.retry.count", properties.getProperty("zk.retry.count")));
    }

    public static String getZkUri() {
        return getOverrideOrDefault("zk.uri", properties.getProperty("zk.uri"));
    }

    public static int getServerPort() {
        return Integer.parseInt(getOverrideOrDefault("me.port", properties.getProperty("me.port")));
    }

    public static long getLogId() {
        return Long.parseLong(getOverrideOrDefault("me.log.id", properties.getProperty("me.log.id")));
    }

    public static Map<String, URI> getReplicaInfo() {
        List<String> keys = List.of(getOverrideOrDefault("me.replica.id", properties.getProperty("me.replica.id")).split(","));
        List<String> values = List.of(getOverrideOrDefault("me.replica.uri", properties.getProperty("me.replica.uri")).split(","));

        return IntStream.range(0, keys.size()).boxed().collect(toMap(keys::get, i -> {
            try {
                return new URI(values.get(i));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
