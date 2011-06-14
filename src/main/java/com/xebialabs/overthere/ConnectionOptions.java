/*
 * This file is part of Overthere.
 * 
 * Overthere is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Overthere is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Overthere.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere;

import java.util.HashMap;
import java.util.Map;

public class ConnectionOptions {
	private Map<String, Object> options = new HashMap<String, Object>();

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

	public static final String TEMPORARY_DIRECTORY_PATH = "tmp";

	public static final String TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT = "tmp.deleteOnDisconnect";

	public static final String OPERATING_SYSTEM = "os";

	public static final String ADDRESS = "address";

	public static final String PORT = "port";

	public static final String USERNAME = "username";

	public static final String PASSWORD = "password";
	
}
