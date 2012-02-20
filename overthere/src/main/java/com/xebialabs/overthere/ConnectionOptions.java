/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Represents options to use when creating a {@link OverthereConnection connection}.
 */
public class ConnectionOptions {

	/**
	 * Connection option for all protocols that specifies the operating system to connect to. This is used to determine how to encode paths and commands and to
	 * determine the default temporary directory path.
	 */
	public static final String OPERATING_SYSTEM = "os";

	/**
	 * Connection option for most protocols that specifies the directory in which the connection-scope temporary directory is created. Any
	 * {@link OverthereConnection#getTempFile(String)} temporary file created, will be created in this directory.
	 */
	public static final String TEMPORARY_DIRECTORY_PATH = "tmp";

	/**
	 * Connection option (Boolean) for most protocols that specifies whether to delete the connection-scope temporary directory when the connection is closed.
	 */
	public static final String TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT = "tmpDeleteOnDisconnect";

	/**
	 * Default value (true) for the connection option that specifies whether to delete the connection-scope temporary directory when the connection is closed.
	 */
	public static final boolean DEFAULT_TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT = true;

	/**
	 * Connection option (int) for most protocol that specifies how many times to retry creating a unique temporary file name before giving up. 
	 */
	public static final String TEMPORARY_FILE_CREATION_RETRIES = "tmpFileCreationRetries";

	/**
	 * Defalut value (100) for connection option that specifies how many times to retry creating a unique temporary file name before giving up. 
	 */
	public static final int DEFAULT_TEMPORARY_FILE_CREATION_RETRIES = 100;

	/**
	 * Connection option (Integer) for all protocol that specifies the connection timeout in milliseconds to use.
	 */
	public static final String CONNECTION_TIMEOUT_MILLIS = "connectionTimeoutMillis";
	
	/**
	 * Default value (120000) for the connection option that specifies the connection timeout in milliseconds to use.
	 */
	public static final int DEFAULT_CONNECTION_TIMEOUT_MILLIS = 120000;

	/**
	 * Common connection option that specifies the address to connect to.
	 */
	public static final String ADDRESS = "address";

	/**
	 * Common connection option that specifies the port to connect to.
	 */
	public static final String PORT = "port";

	/**
	 * Common connection option that specifies the username with which to connect.
	 */
	public static final String USERNAME = "username";

	/**
	 * Common connection option that specifies the password with which to connect.
	 */
	public static final String PASSWORD = "password";

	/**
	 * Common connection option that specifies the connection options used to create a tunnel.
	 */
	public static final String JUMPSTATION = "jumpstation";

	private final Map<String, Object> options;

	/**
	 * Creates an empty options object.
	 */
	public ConnectionOptions() {
		options = newHashMap();
	}

	/**
	 * Creates a copy of an existing options object.
	 */
	public ConnectionOptions(ConnectionOptions options) {
		this.options = newHashMap(options.options);
	}

	/**
	 * Sets a connection option.
	 * 
	 * @param key
	 *            the key of the connection option.
	 * @param value
	 *            the value of the connection option.
	 */
	public void set(String key, Object value) {
		options.put(key, value);
	}

	/**
	 * Retrieves the value of a required connection option.
	 * 
	 * @param <T>
	 *            the type of the connection option.
	 * @param key
	 *            the key of the connection option.
	 * @return the value of the connection option.
	 * @throws IllegalArgumentException if no value was supplied for the connection option
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String key) throws IllegalArgumentException {
		T value = (T) options.get(key);
		if(value == null) {
			throw new IllegalArgumentException("No value specified for required connection option " + key);
		}
		return value;
	}

	/**
	 * Retrieves the value of an optional connection option.
	 * 
	 * @param <T>
	 *            the type of the connection option.
	 * @param key
	 *            the key of the connection option.
	 * @return the value of the connection option or <code>null</code> if that option was not specified.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getOptional(String key) {
		return (T) options.get(key);
	}

	/**
	 * Retrieves the value of a connection option or a default value if that option has not been set.
	 * 
	 * @param <T>
	 *            the type of the connection option.
	 * @param key
	 *            the key of the connection option.
	 * @param defaultValue
	 *            the default value to use of the connection options has not been set.
	 * @return the value of the connection option or the default value if that option was not specified.
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String key, T defaultValue) {
		if (options.containsKey(key)) {
			return (T) options.get(key);
		} else {
			return defaultValue;
		}
	}

	/**
	 * Returns the keys of all connection options set.
	 *
	 * @return a {@link Set} containing the keys.
	 */
	public Set<String> keys() {
		return options.keySet();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ConnectionOptions that = (ConnectionOptions) o;

		return options.equals(that.options);

	}

	@Override
	public int hashCode() {
		return options.hashCode();
	}
}

