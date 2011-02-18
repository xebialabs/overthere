package com.xebialabs.overthere.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.ConnectionOptions;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * A connection to a remote host using SSH w/ interactive SUDO.
 */
public class SshInteractiveSudoHostConnection extends SshSudoHostConnection {

	public SshInteractiveSudoHostConnection(String type, ConnectionOptions options) {
		super(type, options);
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
