package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static java.lang.String.format;

public class SshSftpFreeSshdConnection extends SshSftpConnection {
    private static Logger logger = LoggerFactory.getLogger(SshSftpWinSshdConnection.class);

    public SshSftpFreeSshdConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper);
        checkArgument(os == WINDOWS, "Cannot create a " + SSH_PROTOCOL + ":%s connection to a host that is not running Windows", sshConnectionType.toString()
                .toLowerCase());
    }

    @Override
    protected String pathToSftpPath(String path) {
        String translatedPath;
        boolean escaped = path.startsWith("\"");
        String rawPath = path;
        if (escaped) {
            rawPath = path.substring(1);
        }

        if (rawPath.length() >= 2 && rawPath.charAt(1) == ':') {
            String pathInDrive = rawPath.substring(2).replace('\\', '/');
            translatedPath = "/" + pathInDrive;
            if (escaped) {
                translatedPath = "\"" + translatedPath;
            }
        } else {
            throw new RuntimeIOException(format("Cannot translate Windows path [%s] to a FreeSshd path because it is not a Windows path", path));
        }
        logger.trace("Translated Windows path [{}] to FreeSshd path [{}]", path, translatedPath);
        return translatedPath;
    }
}