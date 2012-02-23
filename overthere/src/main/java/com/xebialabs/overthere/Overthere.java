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

import com.google.common.io.Closeables;
import com.xebialabs.overthere.spi.AddressPortResolver;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;
import com.xebialabs.overthere.ssh.SshTunnelConnection;
import com.xebialabs.overthere.util.DefaultAddressPortResolver;
import nl.javadude.scannit.Configuration;
import nl.javadude.scannit.Scannit;
import nl.javadude.scannit.scanner.TypeAnnotationScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.collect.Sets.newHashSet;
import static com.xebialabs.overthere.ConnectionOptions.JUMPSTATION;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PASSPHRASE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;

/**
 * Factory object to create {@link OverthereConnection connections}.
 */
@SuppressWarnings("unchecked")
public class Overthere {
	// The "logger" field has to be declared and defined at the top so that the static initializer below can access it
	private static final Logger logger = LoggerFactory.getLogger(Overthere.class);

	private static final AtomicReference<Map<String, Class<? extends OverthereConnectionBuilder>>> protocols = new AtomicReference<Map<String, Class<? extends OverthereConnectionBuilder>>>(
	        new HashMap<String, Class<? extends OverthereConnectionBuilder>>());

	static {
		final Scannit scannit = new Scannit(Configuration.config().scan("com.xebialabs").with(new TypeAnnotationScanner()));
		final Set<Class<?>> protocolClasses = scannit.getTypesAnnotatedWith(Protocol.class);
		for (Class<?> protocol : protocolClasses) {
			if (OverthereConnectionBuilder.class.isAssignableFrom(protocol)) {
				final String name = protocol.getAnnotation(Protocol.class).name();
				Overthere.protocols.get().put(name, (Class<? extends OverthereConnectionBuilder>) protocol);
			} else {
				logger.warn("Skipping class {} because it is not a HostConnectionBuilder.", protocol);
			}
		}
	}

    private Overthere() {
        // should not instantiate
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
	public static OverthereConnection getConnection(String protocol, final ConnectionOptions options) {
		if (!protocols.get().containsKey(protocol)) {
			throw new IllegalArgumentException("Unknown connection protocol " + protocol);
		}

		if (logger.isTraceEnabled()) {
            HashSet<String> filteredKeys = newHashSet(PASSWORD, PASSPHRASE);
            logger.trace("Connection for protocol {} requested with the following connection options:", protocol);
			for (String k : options.keys()) {
				Object v = options.get(k);
				logger.trace("{}={}", k, !filteredKeys.contains(k) ? v : "********");
			}
		}

		ConnectionOptions tunnelOptions = options.get(JUMPSTATION, null);
		AddressPortResolver resolver = new DefaultAddressPortResolver();
		if (tunnelOptions != null) {
			resolver = (SshTunnelConnection) Overthere.getConnection(SSH_PROTOCOL, tunnelOptions);
		}
		try {
			return buildConnection(protocol, options, resolver);
		} catch(RuntimeException exc) {
			Closeables.closeQuietly(resolver);
			throw exc;
		}
	}

	private static OverthereConnection buildConnection(String protocol, ConnectionOptions options, AddressPortResolver resolver) {
		final Class<? extends OverthereConnectionBuilder> connectionBuilderClass = protocols.get().get(protocol);
		try {
			final Constructor<? extends OverthereConnectionBuilder> constructor = connectionBuilderClass.getConstructor(String.class, ConnectionOptions.class, AddressPortResolver.class);
			OverthereConnectionBuilder connectionBuilder = constructor.newInstance(protocol, options, resolver);
			logger.info("Connecting to {}", connectionBuilder);
			OverthereConnection connection = connectionBuilder.connect();
			logger.trace("Connected to {}", connection);
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
