package com.xebialabs.overthere.ssh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.xebialabs.overthere.*;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

/**
 * A connection to a remote host using SSH w/ SUDO.
 */
class SshSudoHostConnection extends SshHostConnection {

	protected String sudoUsername;

	SshSudoHostConnection(String type, ConnectionOptions options) {
		super(type, options);
		this.sudoUsername = options.get("sudoUsername");
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

	private Logger logger = LoggerFactory.getLogger(SshSudoHostConnection.class);
}
