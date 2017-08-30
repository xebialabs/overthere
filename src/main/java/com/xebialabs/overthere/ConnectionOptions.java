/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents options to use when creating a {@link OverthereConnection connection}.
 */
public class ConnectionOptions {

    private static final Set<String> filteredKeys = new HashSet<>();

    public static String registerFilteredKey(String key) {
        filteredKeys.add(key);
        return key;
    }

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#protocol">the online documentation</a>
     */
    public static final String PROTOCOL = "protocol";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#os">the online documentation</a>
     */
    public static final String OPERATING_SYSTEM = "os";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#tmp">the online documentation</a>
     */
    public static final String TEMPORARY_DIRECTORY_PATH = "tmp";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#tmpDeleteOnDisconnect">the online documentation</a>
     */
    public static final String TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT = "tmpDeleteOnDisconnect";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#tmpDeleteOnDisconnect">the online documentation</a>
     */
    public static final boolean TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT_DEFAULT = true;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#tmpFileCreationRetries">the online documentation</a>
     */
    public static final String TEMPORARY_FILE_CREATION_RETRIES = "tmpFileCreationRetries";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#tmpFileCreationRetries">the online documentation</a>
     */
    public static final int TEMPORARY_FILE_CREATION_RETRIES_DEFAULT = 100;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#connectionTimeoutMillis">the online documentation</a>
     */
    public static final String CONNECTION_TIMEOUT_MILLIS = "connectionTimeoutMillis";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#connectionTimeoutMillis">the online documentation</a>
     */
    public static final int CONNECTION_TIMEOUT_MILLIS_DEFAULT = 120000;

	/**
	 * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#socketTimeoutMillis">the online documentation</a>
	 */
	public static final String SOCKET_TIMEOUT_MILLIS = "socketTimeoutMillis";

	/**
	 * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#socketTimeoutMillis">the online documentation</a>
	 */
	public static final int SOCKET_TIMEOUT_MILLIS_DEFAULT = 0;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#address">the online documentation</a>
     */
    public static final String ADDRESS = "address";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#port">the online documentation</a>
     */
    public static final String PORT = "port";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#username">the online documentation</a>
     */
    public static final String USERNAME = "username";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#password">the online documentation</a>
     */
    public static final String PASSWORD = registerFilteredKey("password");

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#jumpstation">the online documentation</a>
     */
    public static final String JUMPSTATION = "jumpstation";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#fileCopyCommandForUnix">the online documentation</a>
     */
    public static final String FILE_COPY_COMMAND_FOR_UNIX = "fileCopyCommandForUnix";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#fileCopyCommandForUnix">the online documentation</a>
     */
    public static final String FILE_COPY_COMMAND_FOR_UNIX_DEFAULT = "cp -p {0} {1}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#directoryCopyCommandForUnix">the online documentation</a>
     */
    public static final String DIRECTORY_COPY_COMMAND_FOR_UNIX = "directoryCopyCommandForUnix";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#directoryCopyCommandForUnix">the online documentation</a>
     */
    public static final String DIRECTORY_COPY_COMMAND_FOR_UNIX_DEFAULT = "cd {1} ; tar -cf - -C {0} . | tar xpf -";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#fileCopyCommandForWindows">the online documentation</a>
     */
    public static final String FILE_COPY_COMMAND_FOR_WINDOWS = "fileCopyCommandForWindows";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#fileCopyCommandForWindows">the online documentation</a>
     */
    public static final String FILE_COPY_COMMAND_FOR_WINDOWS_DEFAULT = "copy {0} {1} /y";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#directoryCopyCommandForWindows">the online documentation</a>
     */
    public static final String DIRECTORY_COPY_COMMAND_FOR_WINDOWS = "directoryCopyCommandForWindows";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#directoryCopyCommandForWindows">the online documentation</a>
     */
    public static final String DIRECTORY_COPY_COMMAND_FOR_WINDOWS_DEFAULT = "xcopy {0} {1} /i /y /s /e /h /q";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#fileCopyCommandForZos">the online documentation</a>
     */
    public static final String FILE_COPY_COMMAND_FOR_ZOS = "fileCopyCommandForZos";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#fileCopyCommandForZos">the online documentation</a>
     */
    public static final String FILE_COPY_COMMAND_FOR_ZOS_DEFAULT = "cp -p {0} {1}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#directoryCopyCommandForZos">the online documentation</a>
     */
    public static final String DIRECTORY_COPY_COMMAND_FOR_ZOS = "directoryCopyCommandForZos";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#directoryCopyCommandForZos">the online documentation</a>
     */
    public static final String DIRECTORY_COPY_COMMAND_FOR_ZOS_DEFAULT = "tar cC {0} . | tar xmC {1}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#remoteCopyBufferSize">the online documentation</a>
     */
    public static final String REMOTE_COPY_BUFFER_SIZE = "remoteCopyBufferSize";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#remoteCopyBufferSize">the online documentation</a>
     */
    public static final int REMOTE_COPY_BUFFER_SIZE_DEFAULT = 64 * 1024; // 64 KB

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#remoteCharacterEncoding">the online documentation</a>
     */
    public static final String REMOTE_CHARACTER_ENCODING = "remoteCharacterEncoding";

    private final Map<String, Object> options;

    /**
     * Creates an empty options object.
     */
    public ConnectionOptions() {
        options = new HashMap<String, Object>();
    }

    /**
     * Creates a copy of an existing options object.
     */
    public ConnectionOptions(ConnectionOptions options) {
        this();
        this.options.putAll(options.options);
    }

    /**
     * Sets a connection option.
     *
     * @param key   the key of the connection option.
     * @param value the value of the connection option.
     */
    public void set(String key, Object value) {
        options.put(key, value);
    }

    /**
     * Retrieves the value of a required connection option.
     *
     * @param <T> the type of the connection option.
     * @param key the key of the connection option.
     * @return the value of the connection option.
     * @throws IllegalArgumentException if no value was supplied for the connection option
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
     * @param <T> the type of the connection option.
     * @param key the key of the connection option.
     * @return the value of the connection option or <code>null</code> if that option was not specified.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOptional(String key) {
        return (T) options.get(key);
    }

    /**
     * Retrieves the value of a connection option or a default value if that option has not been set.
     *
     * @param <T>          the type of the connection option.
     * @param key          the key of the connection option.
     * @param defaultValue the default value to use of the connection options has not been set.
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
        T o = getEnum(key, enumClazz, null);
        if (o == null) {
            throw new IllegalArgumentException("No value specified for required connection option " + key);
        } else {
            return o;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getOptionalEnum(String key, Class<T> enumClazz) {
        return getEnum(key, enumClazz, null);
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
     * @param key the key of the connection option.
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
