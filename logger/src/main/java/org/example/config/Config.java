package org.example.config;

import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.IntStream;

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

	// ZooKeeper configuration
	public static int getZkRetrySleepMs() {
		return Integer.parseInt(properties.getProperty("zk.retry.sleep.ms"));
	}

	public static int getZkRetryCount() {
		return Integer.parseInt(properties.getProperty("zk.retry.count"));
	}

	public static String getZkUri() {
		return properties.getProperty("zk.uri");
	}

	// Server configuration
	public static int getServerPort() {
		return Integer.parseInt(properties.getProperty("me.port"));
	}

	public static long getLogId() {
		return Long.parseLong(properties.getProperty("me.log.id"));
	}

	public static Map<String, URI> getReplicaInfo() {
		List<String> keys = List.of(properties.getProperty("me.replica.id").split(","));
		List<String> values = List.of(properties.getProperty("me.replica.uri").split(","));

		return IntStream.range(0, keys.size()).boxed().collect(toMap(keys::get, i -> {
			try {
				return new URI(values.get(i));
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}));
	}
}
