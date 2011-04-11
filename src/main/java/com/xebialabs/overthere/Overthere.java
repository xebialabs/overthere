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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.xebialabs.overthere.spi.HostConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

/**
 * FIXME: Removed functionality:
 * 
 * - untar -> separate utility method, maybe not in here?
 * 
 * - copy resource to temp file -> add helpers to plugin-api
 * 
 * - copy resource to file -> actually only needed by "copy resource to temp file" method
 * 
 * - unreachable host support/tunneled host session -> needs to be reimplemented in a nice way.
 */
@SuppressWarnings("unchecked")
public class Overthere {
	/**
	 * The default timeout for opening a connection in milliseconds.
	 */
	// FIXME: should this not be moved somewhere else?
	public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 120000;
	private static final Logger logger = LoggerFactory.getLogger(Overthere.class);
	private static final AtomicReference<Map<String, Class<? extends HostConnectionBuilder>>> protocols = new AtomicReference<Map<String, Class<? extends HostConnectionBuilder>>>(
	        Maps.<String, Class<? extends HostConnectionBuilder>> newHashMap());

	static {
		final Reflections reflections = new Reflections("com.xebialabs", new TypeAnnotationsScanner());
		final Set<Class<?>> protocols = reflections.getTypesAnnotatedWith(Protocol.class);
		for (Class<?> protocol : protocols) {
			if (HostConnectionBuilder.class.isAssignableFrom(protocol)) {
				final String name = ((Protocol) protocol.getAnnotation(Protocol.class)).name();
				Overthere.protocols.get().put(name, (Class<? extends HostConnectionBuilder>) protocol);
			} else {
				logger.warn("Skipping class {} because it is not a HostConnectionBuilder.", protocol);
			}
		}
	}

	public static HostConnection getConnection(String type, ConnectionOptions options) {
		if (!protocols.get().containsKey(type)) {
			throw new IllegalArgumentException("Unknown connection type " + type);
		}

		final Class<? extends HostConnectionBuilder> connectionBuilder = protocols.get().get(type);
		try {
			final Constructor<? extends HostConnectionBuilder> constructor = connectionBuilder.getConstructor(String.class, ConnectionOptions.class);
			return constructor.newInstance(type, options).connect();
		} catch (NoSuchMethodException exc) {
			throw new IllegalStateException(connectionBuilder + " does not have a constructor that takes in a String and ConnectionOptions.", exc);
		} catch (IllegalArgumentException exc) {
			throw new IllegalStateException("Could not instantiate " + connectionBuilder, exc);
		} catch (InstantiationException exc) {
			throw new IllegalStateException("Could not instantiate " + connectionBuilder, exc);
		} catch (IllegalAccessException exc) {
			throw new IllegalStateException("Could not instantiate " + connectionBuilder, exc);
		} catch (InvocationTargetException exc) {
			if (exc.getCause() instanceof RuntimeException) {
				throw (RuntimeException) exc.getCause();
			} else {
				throw new IllegalStateException("Could not instantiate " + connectionBuilder, exc);
			}
		}
	}

}
