/*
 * Copyright (c) 2008-2010 XebiaLabs B.V. All rights reserved.
 *
 * Your use of XebiaLabs Software and Documentation is subject to the Personal
 * License Agreement.
 *
 * http://www.xebialabs.com/deployit-personal-edition-license-agreement
 *
 * You are granted a personal license (i) to use the Software for your own
 * personal purposes which may be used in a production environment and/or (ii)
 * to use the Documentation to develop your own plugins to the Software.
 * "Documentation" means the how to's and instructions (instruction videos)
 * provided with the Software and/or available on the XebiaLabs website or other
 * websites as well as the provided API documentation, tutorial and access to
 * the source code of the XebiaLabs plugins. You agree not to (i) lease, rent
 * or sublicense the Software or Documentation to any third party, or otherwise
 * use it except as permitted in this agreement; (ii) reverse engineer,
 * decompile, disassemble, or otherwise attempt to determine source code or
 * protocols from the Software, and/or to (iii) copy the Software or
 * Documentation (which includes the source code of the XebiaLabs plugins). You
 * shall not create or attempt to create any derivative works from the Software
 * except and only to the extent permitted by law. You will preserve XebiaLabs'
 * copyright and legal notices on the Software and Documentation. XebiaLabs
 * retains all rights not expressly granted to You in the Personal License
 * Agreement.
 */

package com.xebialabs.overthere.ssh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.xebialabs.deployit.ci.OperatingSystemFamily;
import com.xebialabs.deployit.exception.RuntimeIOException;
import com.xebialabs.overthere.CommandExecution;
import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;

/**
 * A session to a remote host using SSH w/ SUDO.
 */
public class SshSudoHostSession extends SshHostSession {

	protected String sudoUsername;

	/**
	 * Constructs an SshSudoHostSession
	 * 
	 * @param os
	 *            the operating system of the host
	 * @param temporaryDirectoryPath
	 *            the path of the directory in which to store temporary files
	 * @param host
	 *            the hostname or IP adress of the host
	 * @param port
	 *            the port to connect to
	 * @param username
	 *            the username to connect with
	 * @param password
	 *            the password to connect with
	 * @param sudoUsername
	 *            the username to sudo to
	 */
	public SshSudoHostSession(OperatingSystemFamily os, String temporaryDirectoryPath, String host, int port, String username, String password,
	        String sudoUsername) {
		super(os, temporaryDirectoryPath, host, port, username, password);
		this.sudoUsername = sudoUsername;
		open();
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

	protected HostFile getFile(String hostPath, boolean isTempFile) throws RuntimeIOException {
		return new SshSudoHostFile(this, hostPath, isTempFile);
	}

	@Override
	protected HostFile createSessionTempDirectory(HostFile systemTempDirectory, String name) {
		HostFile f = getFile(systemTempDirectory, name, true);
		if (!f.exists()) {
			f.mkdir();
			return f;
		}
		return null;
	}

	public String toString() {
		return username + "@" + host + ":" + port + " (sudo to " + sudoUsername + ")";
	}

	private Logger logger = LoggerFactory.getLogger(SshSudoHostSession.class);

}
