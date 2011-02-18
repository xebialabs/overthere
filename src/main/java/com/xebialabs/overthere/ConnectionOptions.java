package com.xebialabs.overthere;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class ConnectionOptions {
	private Map<String, Object> options = newHashMap();

	public <T> T get(String key) {
		return (T) options.get(key);
	}

	public void set(String key, Object value) {
		options.put(key, value);
	}
}
