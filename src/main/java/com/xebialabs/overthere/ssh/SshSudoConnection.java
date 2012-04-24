/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_COMMAND_PREFIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_COMMAND_PREFIX_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_OVERRIDE_UMASK;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_OVERRIDE_UMASK_DEFAULT;
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
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;

/**
 * A connection to a Unix host using SSH w/ SUDO.
 */
class SshSudoConnection extends SshScpConnection {

	public static final String NOSUDO_PSEUDO_COMMAND = "nosudo";

	protected String sudoUsername;
	
	protected String sudoCommandPrefix;

	protected boolean sudoQuoteCommand;
	
	protected boolean sudoOverrideUmask;

	public SshSudoConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
		super(type, options, mapper);
		this.sudoUsername = options.get(SUDO_USERNAME);
		this.sudoCommandPrefix = options.get(SUDO_COMMAND_PREFIX, SUDO_COMMAND_PREFIX_DEFAULT);
		this.sudoQuoteCommand = options.get(SUDO_QUOTE_COMMAND, SUDO_QUOTE_COMMAND_DEFAULT);
		this.sudoOverrideUmask = options.get(SUDO_OVERRIDE_UMASK, SUDO_OVERRIDE_UMASK_DEFAULT);
	}

	@Override
	protected CmdLine processCommandLine(final CmdLine commandLine) {
		CmdLine cmd;
		if (startsWithPseudoCommand(commandLine, NOSUDO_PSEUDO_COMMAND)) {
			logger.trace("Not prefixing command line with sudo statement because the " + NOSUDO_PSEUDO_COMMAND + " pseudo command was present, but the pseudo command will be stripped");
			logger.trace("Replacing: {}", commandLine);
			cmd = stripPrefixedPseudoCommand(commandLine);
			logger.trace("With     : {}", cmd);
		} else {
			logger.trace("Prefixing command line with sudo statement");
			logger.trace("Replacing: {}", commandLine);
			boolean nocd = startsWithPseudoCommand(commandLine, NOCD_PSEUDO_COMMAND);
			if(nocd) {
				cmd = stripPrefixedPseudoCommand(commandLine);
			} else {
				cmd = commandLine;
			}
			cmd = prefixWithSudoCommand(cmd);
			if(nocd) {
				cmd = prefixWithPseudoCommand(cmd, NOCD_PSEUDO_COMMAND);
			}
			logger.trace("With     : {}", cmd);
		}
		return super.processCommandLine(cmd);
	}

	protected CmdLine prefixWithSudoCommand(final CmdLine commandLine) {
		CmdLine commandLineWithSudo = new CmdLine();
		addSudoStatement(commandLineWithSudo);
		if (sudoQuoteCommand) {
			commandLineWithSudo.addNested(commandLine);
		} else {
			for (CmdLineArgument a : commandLine.getArguments()) {
				commandLineWithSudo.add(a);
				if (a.toString(os, false).equals("|") || a.toString(os, false).equals(";")) {
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

	@Override
	protected OverthereFile getFile(String hostPath, boolean isTempFile) throws RuntimeIOException {
		return new SshSudoFile(this, hostPath, isTempFile);
	}

    @Override
    public String toString() {
        return "ssh:" + sshConnectionType.toString().toLowerCase() + "://" + username + ":" + sudoUsername + "@" + host + ":" + port;
    }

	private Logger logger = LoggerFactory.getLogger(SshSudoFile.class);

}

