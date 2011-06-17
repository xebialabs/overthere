/*
 * This file is part of Overthere.
 * 
 * Overthere is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Overthere is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Overthere.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere.ssh;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.CmdLineArgument;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.Protocol;

/**
 * A connection to a remote host using SSH w/ SUDO.
 */
@Protocol(name = "ssh_sudo")
public class SshSudoConnection extends SshScpConnection {

	public static final String SUDO_USERNAME = "sudoUsername";

	public static final String SUDO_COMMAND = "sudo";

	public static final String NOSUDO_PSEUDO_COMMAND = "nosudo";

	protected String sudoUsername;

	public SshSudoConnection(String type, ConnectionOptions options) {
		super(type, options);
		this.sudoUsername = options.get(SUDO_USERNAME);
	}

	@Override
	public OverthereProcess startProcess(CmdLine commandLine) {
		checkNotNull(commandLine, "Cannot execute null command line");
		checkArgument(commandLine.getArguments().size() > 0, "Cannot execute empty command line");
		return super.startProcess(processCommandLine(commandLine));
	}

	protected CmdLine processCommandLine(CmdLine commandLine) {
		if (commandLine.getArguments().get(0).toString(false).equals(NOSUDO_PSEUDO_COMMAND)) {
			return stripNosudoCommand(commandLine);
		} else {
			return prefixWithSudoCommand(commandLine);
		}
	}

	protected CmdLine stripNosudoCommand(CmdLine commandLine) {
		CmdLine commandLineWithSudo;
		commandLineWithSudo = new CmdLine().add(commandLine.getArguments().subList(1, commandLine.getArguments().size()));
		return commandLineWithSudo;
	}

	protected CmdLine prefixWithSudoCommand(CmdLine commandLine) {
		CmdLine commandLineWithSudo = new CmdLine();
		addSudoStatement(commandLineWithSudo);
		for (CmdLineArgument a : commandLine.getArguments()) {
			commandLineWithSudo.add(a);
			if (a.toString(false).equals("|") || a.toString(false).equals(";")) {
				addSudoStatement(commandLineWithSudo);
			}
		}
		return commandLineWithSudo;
	}

	protected void addSudoStatement(CmdLine sudoCommandLine) {
		sudoCommandLine.addArgument("sudo");
		sudoCommandLine.addArgument("-u");
		sudoCommandLine.addArgument(sudoUsername);
	}

	protected int noSudoExecute(OverthereProcessOutputHandler handler, CmdLine commandLine) {
		if (logger.isDebugEnabled()) {
			logger.debug("NOT adding sudo statement");
		}

		CmdLine nosudoCommandLine = new CmdLine();
		nosudoCommandLine.addArgument(NOSUDO_PSEUDO_COMMAND);
		nosudoCommandLine.add(commandLine.getArguments());
		return execute(handler, nosudoCommandLine);
	}

	protected OverthereFile getFile(String hostPath, boolean isTempFile) throws RuntimeIOException {
		return new SshSudoFile(this, hostPath, isTempFile);
	}

	@Override
	protected OverthereFile createSessionTempDirectory(OverthereFile systemTempDirectory, String name) {
		OverthereFile f = getFile(systemTempDirectory, name, true);
		if (!f.exists()) {
			f.mkdir();
			return f;
		}
		return null;
	}

	@Override
	public String toString() {
		return type + "://" + username + "/" + sudoUsername + "@" + host + ":" + port;
	}

	private Logger logger = LoggerFactory.getLogger(SshSudoConnection.class);

}
