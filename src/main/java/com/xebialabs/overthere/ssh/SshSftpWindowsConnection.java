package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.spi.AddressPortMapper;

import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;

/**
 * A connection to a WINDOWS host using SSH w/ SFTP.
 */
public class SshSftpWindowsConnection extends SshSftpConnection{

    public SshSftpWindowsConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper);
        checkArgument(os == WINDOWS, "Cannot create a %s connection to a host that is running Windows", protocolAndConnectionType);
    }

    @Override
    protected String pathToSftpPath(String path) {
        return path;
    }
}
