package com.xebialabs.overthere.ssh;


import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;

public class SshZOSConnectionBuilder implements OverthereConnectionBuilder {

    public static final String ZOS_CONNECTION_TYPE = "zosConnectionType";
    private static final Logger log = LoggerFactory.getLogger(SshZOSConnectionBuilder.class);

    private SshConnection connection;

    public SshZOSConnectionBuilder(String type, ConnectionOptions options, AddressPortMapper mapper) {
        SshZosConnectionType zosSshConnectionType = options.getEnum(ZOS_CONNECTION_TYPE, SshZosConnectionType.class);
        if (zosSshConnectionType == SshZosConnectionType.SFTP) {
            log.debug("Creating SFTP connection inside ZOSConnectionBuilder");
            options.set(CONNECTION_TYPE, SshConnectionType.SFTP);
            connection = new SshSftpUnixConnection(type, options, mapper);
        } else if (zosSshConnectionType == SshZosConnectionType.SCP) {
            log.debug("Creating SCP connection inside ZOSConnectionBuilder");
            options.set(CONNECTION_TYPE, SshConnectionType.SCP);
            connection = new SshScpConnection(type, options, mapper);
        } else {
            throw new IllegalArgumentException("Unknown SSH connection type " + zosSshConnectionType);
        }
    }

    SshConnection getSshConnection() {
        return connection;
    }

    @Override
    public OverthereConnection connect() {
        connection.connect();
        return connection;
    }

    @Override
    public String toString() {
        return connection.toString();
    }

}