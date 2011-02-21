package com.xebialabs.overthere;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Maps;
import com.xebialabs.overthere.local.LocalHostConnection;
import com.xebialabs.overthere.ssh.SshInteractiveSudoHostConnection;
import com.xebialabs.overthere.ssh.SshScpHostConnection;
import com.xebialabs.overthere.ssh.SshSftpHostConnection;
import com.xebialabs.overthere.ssh.SshSudoHostConnection;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class Overthere {
	/**
	 * The default timeout for opening a connection in milliseconds.
	 */
	// FIXME: should this not be moved somewhere else?
	public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 120000;
	private static final Logger logger = LoggerFactory.getLogger(Overthere.class);
	private static final AtomicReference<Map<String, Class<? extends HostConnectionBuilder>>> protocols =
			new AtomicReference<Map<String, Class<? extends HostConnectionBuilder>>>(Maps.<String, Class<? extends HostConnectionBuilder>>newHashMap());

	static {
		final Reflections reflections = new Reflections("com.xebialabs", new TypeAnnotationsScanner());
		final Set<Class<?>> protocols = reflections.getTypesAnnotatedWith(Protocol.class);
		for (Class protocol : protocols) {
			if (HostConnectionBuilder.class.isAssignableFrom(protocol)) {
				final String name = ((Protocol) protocol.getAnnotation(Protocol.class)).name();
				Overthere.protocols.get().put(name, protocol);
			} else {
				logger.warn("Skipping class {} because it is not a HostConnectionBuilder.", protocol);
			}
		}
	}

	public static HostConnection getConnection(String type, ConnectionOptions options) {
		if (protocols.get().containsKey(type)) {
			final Class<? extends HostConnectionBuilder> connectionBuilder = protocols.get().get(type);
			try {
				final Constructor<? extends HostConnectionBuilder> constructor = connectionBuilder.getConstructor(String.class, ConnectionOptions.class);
				return constructor.newInstance(type, options).connect();
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException(connectionBuilder + " does not have a constructor that takes in a String and ConnectionOptions.", e);
			} catch (Exception e) {
				throw new IllegalStateException("Could not instantiate " + connectionBuilder, e);
			}
		} else {
			throw new IllegalArgumentException("Unknown connection type " + type);
		}
	}

}
