package com.xebialabs.overthere.ssh;

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static java.lang.Character.toLowerCase;

import java.util.List;

import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.CmdLineArgument;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * A connection to a Windows host running OpenSSH on Cygwin using SSH w/ SFTP.
 */
class SshSftpCygwinConnection extends SshSftpConnection {

	public SshSftpCygwinConnection(String type, ConnectionOptions options) {
	    super(type, options);
		checkArgument(os == WINDOWS, "Cannot start a " + SSH_PROTOCOL + ":%s connection to a non-Windows operating system", sshConnectionType.toString().toLowerCase());
    }

	@Override
    protected String pathToSftpPath(String path) {
		String translatedPath = toCygwinPath(path);
		if(translatedPath == null) {
			throw new RuntimeIOException("Cannot translate path " + path + " to SFTP path because it is not a Windows path or a Cygwin path");
		}
		return translatedPath;
    }

	protected String toCygwinPath(String path) {
	    if(path.length() >= 2 && path.charAt(1) == ':') {
			char driveLetter = toLowerCase(path.charAt(0));
			String pathInDrive = path.substring(2).replace('\\', '/');
			String cygwinPath = "/cygdrive/" + driveLetter + pathInDrive;
			logger.trace("Translated Windows path [{}] to Cygdrive path [{}]", path, cygwinPath);
			return cygwinPath;
		} else if(path.startsWith("/cygdrive/")) {
			return path;
		} else {
			return null;
		}
    }

	@Override
	protected CmdLine processCommandLine(final CmdLine commandLine) {
		List<CmdLineArgument> args = commandLine.getArguments();
		checkArgument(args.size() > 0, "Empty command line");

		String arg0 = args.get(0).toString();
		String arg0CygwinPath = toCygwinPath(arg0);
		if(arg0CygwinPath != null) {
			CmdLine modifiedCommandLine = new CmdLine();
			modifiedCommandLine.add(CmdLineArgument.arg(arg0CygwinPath));
			for(int i = 1; i < args.size(); i++) {
				modifiedCommandLine.add(args.get(i));
			}
			logger.debug("Translated first element (command) of command line from Windows path [{}] to Cygwin path [{}]", arg0, arg0CygwinPath);
			return super.processCommandLine(modifiedCommandLine);
		} else {
			return super.processCommandLine(commandLine);
		}
	}

	@Override
	protected SshProcess createProcess(Session session, CmdLine commandLine) throws TransportException, ConnectionException {
    	return new SshProcess(this, UNIX, session, commandLine);
    }

	private Logger logger = LoggerFactory.getLogger(SshSftpCygwinConnection.class);

}
