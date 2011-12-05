/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2011 XebiaLabs
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

import com.xebialabs.overthere.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.*;

/**
 * A connection to a Unix host using SSH w/ SUDO.
 */
class SshSudoConnection extends SshScpConnection {

	public static final String SUDO_COMMAND = "sudo";

	public static final String NOSUDO_PSEUDO_COMMAND = "nosudo";

	protected String sudoUsername;
	
	protected String sudoCommandPrefix;

	protected boolean sudoQuoteCommand;
	
	protected boolean sudoOverrideUmask;

	public SshSudoConnection(String type, ConnectionOptions options) {
		super(type, options);
		this.sudoUsername = options.get(SUDO_USERNAME);
		this.sudoCommandPrefix = options.get(SUDO_COMMAND_PREFIX, SUDO_COMMAND_PREFIX_DEFAULT);
		this.sudoQuoteCommand = options.get(SUDO_QUOTE_COMMAND, SUDO_QUOTE_COMMAND_DEFAULT);
		this.sudoOverrideUmask = options.get(SUDO_OVERRIDE_UMASK, SUDO_OVERRIDE_UMASK_DEFAULT);
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
        return "ssh:" + sshConnectionType.toString().toLowerCase() + "://" + username + ":" + sudoUsername + "@" + host + ":" + port;
    }

	private Logger logger = LoggerFactory.getLogger(SshSudoConnection.class);

}

