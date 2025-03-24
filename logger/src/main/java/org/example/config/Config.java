package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
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

	public static Map<String, String> getReplicaInfo() {
		List<String> keys = Arrays.stream(properties.getProperty("me.replica.id").split(",")).toList();
		List<String> values = Arrays.stream(properties.getProperty("me.replica.uri").split(",")).toList();

		return IntStream.range(0, keys.size()).boxed().collect(Collectors.toMap(keys::get, values::get));
	}
}
