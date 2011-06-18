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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import nl.javadude.scannit.Configuration;
import nl.javadude.scannit.Scannit;
import nl.javadude.scannit.scanner.TypeAnnotationScanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

/**
 * Factory object to create {@link OverthereConnection connections}.
 */
@SuppressWarnings("unchecked")
public class Overthere {
	private static final Logger logger = LoggerFactory.getLogger(Overthere.class);

	private static final AtomicReference<Map<String, Class<? extends OverthereConnectionBuilder>>> protocols = new AtomicReference<Map<String, Class<? extends OverthereConnectionBuilder>>>(
	        new HashMap<String, Class<? extends OverthereConnectionBuilder>>());

	static {
		final Scannit scannit = new Scannit(Configuration.config().scan("com.xebialabs").with(new TypeAnnotationScanner()));
		final Set<Class<?>> protocols = scannit.getTypesAnnotatedWith(Protocol.class);
		for (Class<?> protocol : protocols) {
			if (OverthereConnectionBuilder.class.isAssignableFrom(protocol)) {
				final String name = ((Protocol) protocol.getAnnotation(Protocol.class)).name();
				Overthere.protocols.get().put(name, (Class<? extends OverthereConnectionBuilder>) protocol);
			} else {
				logger.warn("Skipping class {} because it is not a HostConnectionBuilder.", protocol);
			}
		}
	}

	/**
	 * Creates a connection.
	 * 
	 * @param protocol
	 *            The protocol to use, e.g. "local".
	 * @param options
	 *            A set of options to use for the connection.
	 * @return the connection.
	 */
	public static OverthereConnection getConnection(String protocol, ConnectionOptions options) {
		if (!protocols.get().containsKey(protocol)) {
			throw new IllegalArgumentException("Unknown connection protocol " + protocol);
		}

		final Class<? extends OverthereConnectionBuilder> connectionBuilderClass = protocols.get().get(protocol);
		try {
			final Constructor<? extends OverthereConnectionBuilder> constructor = connectionBuilderClass.getConstructor(String.class, ConnectionOptions.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Opening connection with protocol " + protocol);
			}
			OverthereConnectionBuilder connectionBuilder = constructor.newInstance(protocol, options);
			logger.info("Connecing to {}", connectionBuilder);
			OverthereConnection connection = connectionBuilder.connect();
			return connection;
		} catch (NoSuchMethodException exc) {
			throw new IllegalStateException(connectionBuilderClass + " does not have a constructor that takes in a String and ConnectionOptions.", exc);
		} catch (IllegalArgumentException exc) {
			throw new IllegalStateException("Cannot instantiate " + connectionBuilderClass, exc);
		} catch (InstantiationException exc) {
			throw new IllegalStateException("Cannot instantiate " + connectionBuilderClass, exc);
		} catch (IllegalAccessException exc) {
			throw new IllegalStateException("Cannot instantiate " + connectionBuilderClass, exc);
		} catch (InvocationTargetException exc) {
			if (exc.getCause() instanceof RuntimeException) {
				throw (RuntimeException) exc.getCause();
			} else {
				throw new IllegalStateException("Cannot instantiate " + connectionBuilderClass, exc);
			}
		}
	}

}
