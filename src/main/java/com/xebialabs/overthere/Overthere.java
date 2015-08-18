/*
 * Copyright (c) 2008-2014, XebiaLabs B.V., All rights reserved.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xebialabs.overthere.local.LocalConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;
import com.xebialabs.overthere.ssh.SshTunnelConnection;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;

import nl.javadude.scannit.Configuration;
import nl.javadude.scannit.Scannit;
import nl.javadude.scannit.scanner.TypeAnnotationScanner;

import static com.xebialabs.overthere.ConnectionOptions.JUMPSTATION;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;

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
        if (Scannit.isBooted()) {
            logger.info("Scannit already booted, checking to see whether it has scanned 'com.xebialabs'");
            Set<Class<?>> protocols = Scannit.getInstance().getTypesAnnotatedWith(Protocol.class);
            if (!protocols.isEmpty()) {
                boot(Scannit.getInstance());
            } else {
                boot();
            }
        } else {
            boot();
        }
    }

    private static void boot() {
        boot(new Scannit(Configuration.config().scan("com.xebialabs").with(new TypeAnnotationScanner())));
    }

    private static void boot(Scannit scannit) {
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
     * @param protocol The protocol to use, e.g. "local".
     * @param options  A set of options to use for the connection.
     * @return the connection.
     */
    public static OverthereConnection getConnection(String protocol, final ConnectionOptions options) {
        if (!protocols.get().containsKey(protocol)) {
            throw new IllegalArgumentException("Unknown connection protocol " + protocol);
        }

        logger.trace("Connection for protocol {} requested with the following connection options: {}", protocol, options);

        ConnectionOptions jumpstationOptions = options.getOptional(JUMPSTATION);
        AddressPortMapper mapper = DefaultAddressPortMapper.INSTANCE;
        if (jumpstationOptions != null) {
            mapper = (SshTunnelConnection) Overthere.getConnection(SSH_PROTOCOL, jumpstationOptions);
        }
        try {
            return buildConnection(protocol, options, mapper);
        } catch (RuntimeException exc) {
            closeQuietly(mapper);
            throw exc;
        }
    }

    private static OverthereConnection buildConnection(String protocol, ConnectionOptions options, AddressPortMapper mapper) {
        final Class<? extends OverthereConnectionBuilder> connectionBuilderClass = protocols.get().get(protocol);
        try {
            final Constructor<? extends OverthereConnectionBuilder> constructor = connectionBuilderClass.getConstructor(String.class, ConnectionOptions.class, AddressPortMapper.class);
            OverthereConnectionBuilder connectionBuilder = constructor.newInstance(protocol, options, mapper);

            if (connectionBuilder instanceof LocalConnection) {
                logger.debug("Connecting to {}", connectionBuilder);
            } else {
                logger.info("Connecting to {}", connectionBuilder);
            }

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
