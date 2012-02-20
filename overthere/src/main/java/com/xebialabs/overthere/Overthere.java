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

import static com.google.common.collect.Sets.newHashSet;
import static com.xebialabs.overthere.ConnectionOptions.JUMPSTATION;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PASSPHRASE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
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
import com.xebialabs.overthere.ssh.SshTunnelConnection;
import com.xebialabs.overthere.ssh.SshTunnelRegistry;

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
		ConnectionOptions rewrittenOptions = options;
		if (tunnelOptions != null) {
			SshTunnelConnection tunnel = (SshTunnelConnection) buildConnection(SSH_PROTOCOL, tunnelOptions);
			rewrittenOptions = tunnel.rewriteAddressAndPorts(options);
		}
		try {
			return buildConnection(protocol, rewrittenOptions);
		} catch(RuntimeException exc) {
			if(tunnelOptions != null) {
				SshTunnelRegistry.closeTunnel(tunnelOptions);
			}
			throw exc;
		}
	}

	private static OverthereConnection buildConnection(String protocol, ConnectionOptions options) {
		final Class<? extends OverthereConnectionBuilder> connectionBuilderClass = protocols.get().get(protocol);
		try {
			final Constructor<? extends OverthereConnectionBuilder> constructor = connectionBuilderClass.getConstructor(String.class, ConnectionOptions.class);
			OverthereConnectionBuilder connectionBuilder = constructor.newInstance(protocol, options);
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

