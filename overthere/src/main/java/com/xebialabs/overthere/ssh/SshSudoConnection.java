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

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_COMMAND_PREFIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_COMMAND_PREFIX_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_QUOTE_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_QUOTE_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_USERNAME;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.CmdLineArgument;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * A connection to a remote host using SSH w/ SUDO.
 */
class SshSudoConnection extends SshScpConnection {

	public static final String SUDO_COMMAND = "sudo";

	public static final String NOSUDO_PSEUDO_COMMAND = "nosudo";

	protected String sudoUsername;
	
	protected String sudoCommandPrefix;

	protected boolean sudoQuoteCommand;

	public SshSudoConnection(String type, ConnectionOptions options) {
		super(type, options);
		this.sudoUsername = options.get(SUDO_USERNAME);
		this.sudoCommandPrefix = options.get(SUDO_COMMAND_PREFIX, SUDO_COMMAND_PREFIX_DEFAULT);
		this.sudoQuoteCommand = options.get(SUDO_QUOTE_COMMAND, SUDO_QUOTE_COMMAND_DEFAULT);
	}

	@Override
	protected CmdLine processCommandLine(final CmdLine commandLine) {
		CmdLine cmd;
		if (commandLine.getArguments().get(0).toString(os, false).equals(NOSUDO_PSEUDO_COMMAND)) {
			cmd = stripNosudoCommand(commandLine);
		} else {
			cmd = prefixWithSudoCommand(commandLine);
		}
		return super.processCommandLine(cmd);
	}

	protected CmdLine stripNosudoCommand(final CmdLine commandLine) {
		return new CmdLine().add(commandLine.getArguments().subList(1, commandLine.getArguments().size()));
	}

	protected CmdLine prefixWithSudoCommand(final CmdLine commandLine) {
		CmdLine commandLineWithSudo = new CmdLine();
		addSudoStatement(commandLineWithSudo);
		if (sudoQuoteCommand) {
			commandLineWithSudo.addNested(commandLine);
		} else {
			for (CmdLineArgument a : commandLine.getArguments()) {
				commandLineWithSudo.add(a);
				if (a.toString(os, false).equals("|") || a.toString(os, false).equals("\\;")) {
					addSudoStatement(commandLineWithSudo);
				}
			}
		}
		return commandLineWithSudo;
	}

	protected void addSudoStatement(CmdLine sudoCommandLine) {
		String prefix = MessageFormat.format(sudoCommandPrefix, sudoUsername);
		for(String arg : prefix.split("\\s+")) {
			sudoCommandLine.addArgument(arg);
		}
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
	public String toString() {
		return type + "://" + username + "@" + host + ":" + port + " (sudo to " + sudoUsername + ")";
	}

	private Logger logger = LoggerFactory.getLogger(SshSudoConnection.class);

}
