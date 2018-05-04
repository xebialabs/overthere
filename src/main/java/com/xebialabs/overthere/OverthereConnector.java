package com.xebialabs.overthere;

import com.xebialabs.overthere.local.LocalConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.xebialabs.overthere.ConnectionOptions.JUMPSTATION;
import static com.xebialabs.overthere.ConnectionOptions.PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshJumpstationConnectionBuilder.SSH_JUMPSTATION_PROTOCOL;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;

public class OverthereConnector {
    private static final Logger logger = LoggerFactory.getLogger(OverthereConnector.class);

    final AtomicReference<Map<String, Class<? extends OverthereConnectionBuilder>>> protocols = new AtomicReference<Map<String, Class<? extends OverthereConnectionBuilder>>>(
            new HashMap<String, Class<? extends OverthereConnectionBuilder>>());


    public void registerProtocol(Class<? extends OverthereConnectionBuilder> builderClass) {
        if (!builderClass.isAnnotationPresent(Protocol.class)) {
            throw new IllegalArgumentException("The OverthereConnectionBuilder " + builderClass + " should be annotated with @Protocol");
        }
        final String name = builderClass.getAnnotation(Protocol.class).name();
        protocols.get().put(name, builderClass);

    }
    /**
     * Creates a connection.
     *
     * @param protocol The protocol to use, e.g. "local".
     * @param options  A set of options to use for the connection.
     * @return the connection.
     */
    public OverthereConnection getConnection(String protocol, final ConnectionOptions options) {
        if (!protocols.get().containsKey(protocol)) {
            throw new IllegalArgumentException("Unknown connection protocol " + protocol);
        }

        logger.trace("Connection for protocol {} requested with the following connection options: {}", protocol, options);

        ConnectionOptions jumpstationOptions = options.getOptional(JUMPSTATION);
        AddressPortMapper mapper = DefaultAddressPortMapper.INSTANCE;
        if (jumpstationOptions != null) {
            // For backwards compatibility, SSH-jumpstation is the default protocol.
            String jumpstationProtocol = jumpstationOptions.get(PROTOCOL, SSH_JUMPSTATION_PROTOCOL);

            if(jumpstationProtocol.equals(SSH_PROTOCOL)) {
                // If the protocol is specified as "ssh", use "ssh-jumpstation" instead.
                jumpstationProtocol = SSH_JUMPSTATION_PROTOCOL;
            }

            mapper = (AddressPortMapper) Overthere.getConnection(jumpstationProtocol, jumpstationOptions);
        }

        try {
            return buildConnection(protocol, options, mapper);
        } catch (RuntimeException exc) {
            closeQuietly(mapper);
            throw exc;
        }
    }

    private OverthereConnection buildConnection(String protocol, ConnectionOptions options, AddressPortMapper mapper) {
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
            throw new IllegalStateException(connectionBuilderClass + " does not have a public constructor with the signature (String, ConnectionOptions, AddressPortMapper)", exc);
        } catch (IllegalAccessException| IllegalArgumentException | InstantiationException exc) {
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
