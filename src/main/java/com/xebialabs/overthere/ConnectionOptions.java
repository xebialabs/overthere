package com.xebialabs.overthere;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class ConnectionOptions {
	private Map<String, Object> options = newHashMap();

	@SuppressWarnings("unchecked")
    public <T> T get(String key) {
		return (T) options.get(key);
	}

	@SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
		if (options.containsKey(key)) {
			return (T) options.get(key);
		} else {
			return defaultValue;
		}
	}

	public void set(String key, Object value) {
		options.put(key, value);
	}
}
