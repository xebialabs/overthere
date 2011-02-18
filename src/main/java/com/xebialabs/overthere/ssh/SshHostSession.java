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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.xebialabs.overthere.BrokenHostSessionFactory.DEFAULT_CONNECTION_TIMEOUT_MS;
import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.xebialabs.overthere.AbortedException;
import com.xebialabs.overthere.CommandExecution;
import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;
import com.xebialabs.overthere.HostSession;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.common.AbstractHostSession;
import com.xebialabs.overthere.common.ErrorStreamToCallbackHandler;
import com.xebialabs.overthere.common.InputResponseHandler;
import com.xebialabs.overthere.common.OutputStreamToCallbackHandler;

/**
 * A host session over SSH.
 */
abstract class SshHostSession extends AbstractHostSession implements HostSession {

	protected String host;

	protected int port;

	protected String username;

	protected String password;

	protected Session sharedSession;

	private static final String CHANNEL_PURPOSE = "";

	/**
	 * Constructs an SshHostSession
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
	 */
	public SshHostSession(OperatingSystemFamily os, String temporaryDirectoryPath, String host, int port, String username, String password) {
		super(os, temporaryDirectoryPath);
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	void open() throws RuntimeIOException {
		if (sharedSession == null) {
			try {
				sharedSession = openSession(CHANNEL_PURPOSE);
			} catch (JSchException exc) {
				throw new RuntimeIOException("Cannot connect to " + this, exc);
			}
		}
	}

	@Override
	public void close() {
		super.close();
		disconnectSharedSession();
	}

	protected Session getSharedSession() {
		if (sharedSession == null) {
			throw new IllegalStateException("Not connected");
		}
		return sharedSession;
	}

	public void disconnectSharedSession() {
		disconnectSession(sharedSession, CHANNEL_PURPOSE);
		sharedSession = null;
	}

	protected Session openSession(String purpose) throws JSchException {
		JSch jsch = new JSch();

		final String privateKeyFilename = System.getProperty("ssh.privatekey.filename");
		if (privateKeyFilename != null) {
			logger.info(format("found in System properties a private key filename '%s'", privateKeyFilename));
			jsch.addIdentity(privateKeyFilename, System.getProperty("ssh.privatekey.passphrase"));
		}

		Session session = jsch.getSession(username, host, port);

		session.setUserInfo(getUserInfo());
		session.connect(DEFAULT_CONNECTION_TIMEOUT_MS);
		logger.info("Connected to " + this + purpose);
		return session;
	}

	protected void disconnectSession(Session session, String purpose) {
		if (session != null) {
			session.disconnect();
			logger.info("Disconnected from " + this + purpose);
		}
	}

	public HostFile getFile(String hostPath) throws RuntimeIOException {
		return getFile(hostPath, false);
	}

	protected abstract HostFile getFile(String hostPath, boolean isTempFile) throws RuntimeIOException;

	public HostFile getFile(HostFile parent, String child) throws RuntimeIOException {
		return getFile(parent, child, false);
	}

	protected HostFile getFile(HostFile parent, String child, boolean isTempFile) throws RuntimeIOException {
		if (!(parent instanceof SshHostFile)) {
			throw new IllegalStateException("parent is not a file on an SSH host");
		}
		if (parent.getSession() != this) {
			throw new IllegalStateException("parent is not a file in this session");
		}
		return getFile(parent.getPath() + getHostOperatingSystem().getFileSeparator() + child, isTempFile);
	}

	public HostFile getTempFile(String prefix, String suffix) throws RuntimeIOException {
		checkNotNull(prefix);
		if (suffix == null) {
			suffix = ".tmp";
		}

		Random r = new Random();
		String infix = "";
		for (int i = 0; i < AbstractHostSession.MAX_TEMP_RETRIES; i++) {
			HostFile f = getFile(getTemporaryDirectory().getPath() + getHostOperatingSystem().getFileSeparator() + prefix + infix + suffix, true);
			if (!f.exists()) {
				if (logger.isDebugEnabled())
					logger.debug("Created temporary file " + f);

				return f;
			}
			infix = "-" + Long.toString(Math.abs(r.nextLong()));
		}
		throw new RuntimeIOException("Cannot generate a unique temporary file name on " + this);
	}

	@SuppressWarnings("unchecked")
	public int execute(CommandExecutionCallbackHandler handler, String... commandLine) throws RuntimeIOException {
		return execute(handler, Collections.EMPTY_MAP, commandLine);
	}

	public int execute(CommandExecutionCallbackHandler handler, Map<String, String> inputResponse, String... cmdarray) throws RuntimeIOException {
		String commandLineForExecution = encodeCommandLineForExecution(cmdarray);
		String commandLineForLogging = encodeCommandLineForLogging(cmdarray);

		try {
			ChannelExec channel = createExecChannel();
			Thread outputCopierThread = null;
			Thread errorCopierThread = null;
			try {
				// set up command
				channel.setCommand(commandLineForExecution);

				// set up streams
				InputStream remoteStdout = channel.getInputStream();
				InputStream remoteStderr = channel.getErrStream();
				OutputStream remoteStdin = channel.getOutputStream();

				// prepare to capture output
				CommandExecutionCallbackHandler responseHandler = getInputResponseHandler(handler, remoteStdin, inputResponse);
				outputCopierThread = new Thread(new OutputStreamToCallbackHandler(remoteStdout, responseHandler));
				outputCopierThread.start();
				errorCopierThread = new Thread(new ErrorStreamToCallbackHandler(remoteStderr, responseHandler));
				errorCopierThread.start();

				// execute the command
				channel.connect();
				logger.info("Executing remote command \"" + commandLineForLogging + "\" on " + this);

				int exitValue = waitForExitStatus(channel, commandLineForLogging);
				if (logger.isDebugEnabled())
					logger.debug("Finished executing remote command \"" + commandLineForLogging + "\" on " + this + " with exit value " + exitValue);
				return exitValue;
			} finally {
				channel.disconnect();
				if (outputCopierThread != null) {
					try {
						outputCopierThread.join();
					} catch (InterruptedException ignored) {
					}
				}
				if (errorCopierThread != null) {
					try {
						errorCopierThread.join();
					} catch (InterruptedException ignored) {
					}
				}
			}
		} catch (IOException exc) {
			throw new RuntimeIOException("Cannot execute remote command \"" + commandLineForLogging + "\" on " + this, exc);
		} catch (JSchException exc) {
			throw new RuntimeIOException("Cannot execute remote command \"" + commandLineForLogging + "\" on " + this, exc);
		}
	}

	protected ChannelExec createExecChannel() throws JSchException {
		ChannelExec channel = (ChannelExec) getSharedSession().openChannel("exec");
		return channel;
	}

	protected CommandExecutionCallbackHandler getInputResponseHandler(CommandExecutionCallbackHandler originalHandler, OutputStream remoteStdin,
	        Map<String, String> inputResponse) {
		return new InputResponseHandler(originalHandler, remoteStdin, inputResponse);
	}

	public CommandExecution startExecute(String... cmdarray) {
		final String commandLineForExecution = encodeCommandLineForExecution(cmdarray);
		final String commandLineForLogging = encodeCommandLineForLogging(cmdarray);
		try {
			final ChannelExec channel = createExecChannel();
			// set up command
			channel.setCommand(commandLineForExecution);

			channel.connect();

			logger.info("Executing remote command \"" + commandLineForLogging + "\" on " + this + " and passing control to caller");

			return getCommandExecution(commandLineForLogging, channel);
		} catch (JSchException exc) {
			throw new RuntimeIOException("Cannot execute remote command \"" + commandLineForLogging + "\" on " + this, exc);
		}

	}

	protected ChannelExecCommandExecution getCommandExecution(String command, ChannelExec channel) {
		return new ChannelExecCommandExecution(channel, command);
	}

	protected static class ChannelExecCommandExecution implements CommandExecution {
		private final ChannelExec channel;
		private final String command;

		protected ChannelExecCommandExecution(ChannelExec channel, String command) {
			this.channel = channel;
			this.command = command;
		}

		public OutputStream getStdin() {
			try {
				return channel.getOutputStream();
			} catch (IOException exc) {
				throw new RuntimeIOException("Cannot open output stream to remote stdin");
			}
		}

		public InputStream getStdout() {
			try {
				return channel.getInputStream();
			} catch (IOException exc) {
				throw new RuntimeIOException("Cannot open input stream from remote stdout");
			}
		}

		public InputStream getStderr() {
			try {
				return channel.getErrStream();
			} catch (IOException exc) {
				throw new RuntimeIOException("Cannot open input stream from remote stderr");
			}
		}

		public int waitFor() {
			try {
				int exitValue = waitForExitStatus(channel, command);
				logger.info("Finished executing remote command \"" + command + "\" on " + this + " with exit value " + exitValue
				        + " (control was passed to caller)");
				return exitValue;
			} finally {
				channel.disconnect();
			}
		}
	}

	static int waitForExitStatus(ChannelExec channel, String command) {
		while (true) {
			if (channel.isClosed()) {
				return channel.getExitStatus();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException exc) {
				Thread.currentThread().interrupt();
				throw new AbortedException("Remote command \"" + command + "\" was interrupted", exc);
			}
		}
	}

	protected UserInfo getUserInfo() {
		return new UserInfo() {
			public boolean promptPassword(String prompt) {
				return true;
			}

			public String getPassword() {
				return password;
			}

			public boolean promptPassphrase(String prompt) {
				return false;
			}

			public String getPassphrase() {
				return null;
			}

			public boolean promptYesNo(String prompt) {
				return true;
			}

			public void showMessage(String msg) {
				logger.info("Message recieved while connecting to " + username + "@" + host + ":" + port + ": " + msg);
			}
		};
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String toString() {
		return username + "@" + host + ":" + port;
	}

	private static Logger logger = LoggerFactory.getLogger(SshHostSession.class);

}
