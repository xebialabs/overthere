package com.xebialabs.overthere.ssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.xebialabs.deployit.ci.OperatingSystemFamily;
import com.xebialabs.overthere.CommandExecutionCallbackHandler;

/**
 * A session to a remote host using SSH w/ interactive SUDO.
 */
public class SshInteractiveSudoHostSession extends SshSudoHostSession {

	/**
	 * Constructs an SshInteractiveSudoHostSession
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
	public SshInteractiveSudoHostSession(OperatingSystemFamily os, String temporaryDirectoryPath, String host, int port, String username, String password,
	        String sudoUsername) {
		super(os, temporaryDirectoryPath, host, port, username, password, sudoUsername);
	}

	@Override
	protected ChannelExec createExecChannel() throws JSchException {
		ChannelExec channel = super.createExecChannel();
		channel.setPty(true);
		return channel;
	}

	@Override
	protected CommandExecutionCallbackHandler getInputResponseHandler(CommandExecutionCallbackHandler originalHandler, OutputStream remoteStdin,
	        Map<String, String> inputResponse) {
		return new SshInteractiveSudoPasswordPromptHandler(super.getInputResponseHandler(originalHandler, remoteStdin, inputResponse), remoteStdin,
		        getPassword());
	}

	@Override
	protected ChannelExecCommandExecution getCommandExecution(String command, ChannelExec channel) {
		return new ChannelExecCommandExecution(channel, command) {
			@Override
			public InputStream getStdout() {
				return new SshInteractiveSudoPasswordHandlingStream(super.getStdout(), getStdin(), password);
			}
		};
	}

}
