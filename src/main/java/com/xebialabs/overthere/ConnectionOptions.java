/**
 * Copyright (c) 2008, 2012, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere;

import static com.google.common.collect.Maps.newHashMap;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PASSPHRASE;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Represents options to use when creating a {@link OverthereConnection connection}.
 */
public class ConnectionOptions {

    /**
     * Connection option for all protocols that specifies the operating system to connect to. This is used to determine
     * how to encode paths and commands and to determine the default temporary directory path.
     */
    public static final String OPERATING_SYSTEM = "os";

    /**
     * Connection option for most protocols that specifies the directory in which the connection-scope temporary
     * directory is created. Any {@link OverthereConnection#getTempFile(String)} temporary file created, will be created
     * in this directory.
     */
    public static final String TEMPORARY_DIRECTORY_PATH = "tmp";

    /**
     * Connection option (Boolean) for most protocols that specifies whether to delete the connection-scope temporary
     * directory when the connection is closed.
     */
    public static final String TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT = "tmpDeleteOnDisconnect";

    /**
     * Default value (true) for the connection option that specifies whether to delete the connection-scope temporary
     * directory when the connection is closed.
     */
    public static final boolean DEFAULT_TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT = true;

    /**
     * Connection option (int) for most protocol that specifies how many times to retry creating a unique temporary file
     * name before giving up.
     */
    public static final String TEMPORARY_FILE_CREATION_RETRIES = "tmpFileCreationRetries";

    /**
     * Defalut value (100) for connection option that specifies how many times to retry creating a unique temporary file
     * name before giving up.
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

    private static final ImmutableSet<String> filteredKeys = ImmutableSet.of(PASSWORD, PASSPHRASE);

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
     * @throws IllegalArgumentException
     *             if no value was supplied for the connection option
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) throws IllegalArgumentException {
        T value = (T) options.get(key);
        if (value == null) {
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

    public boolean getBoolean(String key) {
        Object o = options.get(key);
        if (o == null) {
            throw new IllegalArgumentException("No value specified for required connection option " + key);
        } else if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof String) {
            return Boolean.valueOf((String) o);
        } else {
            throw new IllegalArgumentException("Value specified for required connection option " + key + " is neither a Boolean nor a String");
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object o = options.get(key);
        if (o == null) {
            return defaultValue;
        } else if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof String) {
            return Boolean.valueOf((String) o);
        } else {
            throw new IllegalArgumentException("Value specified for connection option " + key + " is neither a Boolean nor a String");
        }
    }

    public int getInteger(String key) {
        Object o = options.get(key);
        if (o == null) {
            throw new IllegalArgumentException("No value specified for required connection option " + key);
        } else if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof String) {
            return Integer.parseInt((String) o);
        } else {
            throw new IllegalArgumentException("Value specified for required connection option " + key + " is neither an Integer nor a String");
        }
    }

    public int getInteger(String key, int defaultValue) {
        Object o = options.get(key);
        if (o == null) {
            return defaultValue;
        } else if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof String) {
            return Integer.parseInt((String) o);
        } else {
            throw new IllegalArgumentException("Value specified for connection option " + key + " is neither an Integer nor a String");
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getEnum(String key, Class<T> enumClazz) {
        Object o = options.get(key);
        if (o == null) {
            throw new IllegalArgumentException("No value specified for required connection option " + key);
        } else if (o.getClass().equals(enumClazz)) {
            return (T) o;
        } else if (o instanceof String) {
            return Enum.valueOf(enumClazz, (String) o);
        } else {
            throw new IllegalArgumentException("Value specified for required connection option " + key + " is neither an " + enumClazz.getName()
                + " nor a String");
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getEnum(String key, Class<T> enumClazz, T defaultValue) {
        Object o = options.get(key);
        if (o == null) {
            return defaultValue;
        } else if (o.getClass().equals(enumClazz)) {
            return (T) o;
        } else if (o instanceof String) {
            return Enum.valueOf(enumClazz, (String) o);
        } else {
            throw new IllegalArgumentException("Value specified for connection option " + key + " is neither an instanceof of " + enumClazz.getName()
                + " nor a String");
        }
    }

    /**
     * Returns whether a connection option is set.
     * 
     * @param key
     *            the key of the connection option.
     * @return true iff the connection option is set, false otherwise.
     */
    public boolean containsKey(String key) {
        return options.containsKey(key);
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ConnectionOptions that = (ConnectionOptions) o;

        return options.equals(that.options);

    }

    @Override
    public int hashCode() {
        return options.hashCode();
    }

    @Override
    public String toString() {
        return print(this, "");
    }

    private static String print(ConnectionOptions options, String indent) {
        StringBuilder b = new StringBuilder();
        b.append("ConnectionOptions[\n");
        for (Map.Entry<String, Object> e : options.options.entrySet()) {
            b.append(indent).append("\t").append(e.getKey()).append(" --> ");
            Object value = e.getValue();
            if (value instanceof ConnectionOptions) {
                b.append(print((ConnectionOptions) value, indent + "\t"));
            } else {
                b.append(filteredKeys.contains(e.getKey()) ? "********" : value);
            }
            b.append("\n");
        }
        b.append(indent).append("]");
        return b.toString();
    }
}
