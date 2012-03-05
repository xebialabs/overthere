package com.xebialabs.overthere.ssh;

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static java.lang.Character.toLowerCase;

import com.xebialabs.overthere.spi.AddressPortMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * A connection to a Windows host running OpenSSH on Cygwin using SSH w/ SFTP.
 */
class SshSftpCygwinConnection extends SshSftpConnection {

	public SshSftpCygwinConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
	    super(type, options, mapper);
		checkArgument(os == WINDOWS, "Cannot start a " + SSH_PROTOCOL + ":%s connection to a non-Windows operating system", sshConnectionType.toString().toLowerCase());
    }

	@Override
    protected void addCommandSeparator(CmdLine commandLine) {
	    // TODO Auto-generated method stub
	    super.addCommandSeparator(commandLine);
    }

	@Override
    protected String pathToSftpPath(String path) {
		String translatedPath;
		if(path.length() >= 2 && path.charAt(1) == ':') {
			char driveLetter = toLowerCase(path.charAt(0));
			String pathInDrive = path.substring(2).replace('\\', '/');
			translatedPath = "/cygdrive/" + driveLetter + pathInDrive;
		} else if(path.startsWith("/cygdrive/")) {
			translatedPath = path;
		} else {
			throw new RuntimeIOException("Cannot translate path " + path + " because it is not a Windows path or a Cygwin path");
		}
		logger.trace("Translated path {} to sftp path {}", path, translatedPath);
		return translatedPath;
    }

	private Logger logger = LoggerFactory.getLogger(SshSftpCygwinConnection.class);

}
