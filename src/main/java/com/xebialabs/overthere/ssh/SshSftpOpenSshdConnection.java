package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static java.lang.Character.toUpperCase;
import static java.lang.String.format;

/**
 * A connection to a Windows host running OpenSSHD.
 */
public class SshSftpOpenSshdConnection extends SshSftpConnection {

    public SshSftpOpenSshdConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper);
        checkArgument(os == WINDOWS, "Cannot create a %s connection to a host that is not running Windows", protocolAndConnectionType);
    }

    @Override
    protected void connect() {
        super.connect();
    }

    @Override
    protected String pathToSftpPath(String path) {
        String translatedPath;
        if (path.length() >= 2 && path.charAt(1) == ':') {
            char driveLetter = toUpperCase(path.charAt(0));
            String pathInDrive = path.substring(2).replace('\\', '/');
            translatedPath = "/" + driveLetter + pathInDrive;
        } else {
            throw new RuntimeIOException(format("Cannot translate Windows path [%s] to a WinSSHD path because it is not a Windows path", path));
        }
        logger.trace("Translated Windows path [{}] to OpenSSHD path [{}]", path, translatedPath);
        return translatedPath;
    }

    private static Logger logger = LoggerFactory.getLogger(SshSftpOpenSshdConnection.class);
}
