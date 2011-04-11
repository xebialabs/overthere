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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.spi.Protocol;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

/**
 * A connection to a remote host using SSH w/ SUDO.
 */
@Protocol(name = "ssh_sudo")
public class SshSudoHostConnection extends SshHostConnection {

	protected String sudoUsername;

	public SshSudoHostConnection(String type, ConnectionOptions options) {
		super(type, options);
		this.sudoUsername = options.get("sudoUsername");
	}

	@Override
	public int execute(CommandExecutionCallbackHandler handler, Map<String, String> inputResponse, String... commandLine) throws RuntimeIOException {
		String[] commandLineWithSudo = prependSudoCommand(commandLine);
		return super.execute(handler, inputResponse, commandLineWithSudo);
	}

	@Override
	public CommandExecution startExecute(String... commandLine) {
		String[] commandLineWithSudo = prependSudoCommand(commandLine);
		return super.startExecute(commandLineWithSudo);
	}

	protected String[] prependSudoCommand(String... commandLine) {
		List<String> sudoCommandLine = new ArrayList<String>();
		for (int i = 0; i < commandLine.length; i++) {
			if (i == 0) {
				addSudoStatement(sudoCommandLine);
			}
			sudoCommandLine.add(commandLine[i]);
			if (commandLine[i].equals("|") || commandLine[i].equals(";")) {
				addSudoStatement(sudoCommandLine);
			}
		}
		String[] commandLineWithSudo = sudoCommandLine.toArray(new String[sudoCommandLine.size()]);
		return commandLineWithSudo;
	}

	protected void addSudoStatement(List<String> sudoCommandLineCollector) {
		sudoCommandLineCollector.add("sudo");
		sudoCommandLineCollector.add("-u");
		sudoCommandLineCollector.add(sudoUsername);
	}
	
	@SuppressWarnings("unchecked")
	protected int noSudoExecute(CommandExecutionCallbackHandler handler, String... commandLine) {
		if (logger.isDebugEnabled())
			logger.debug("NOT adding sudo statement");

		return super.execute(handler, Collections.EMPTY_MAP, commandLine);
	}

	protected OverthereFile getFile(String hostPath, boolean isTempFile) throws RuntimeIOException {
		return new SshSudoOverthereFile(this, hostPath, isTempFile);
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

	public String toString() {
		return username + "@" + host + ":" + port + " (sudo to " + sudoUsername + ")";
	}

	private Logger logger = LoggerFactory.getLogger(SshSudoHostConnection.class);
}

