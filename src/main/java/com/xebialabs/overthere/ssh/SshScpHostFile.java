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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;

import com.xebialabs.deployit.exception.RuntimeIOException;
import com.xebialabs.overthere.CapturingCommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;

/**
 * A file on a host connected through SSH w/ SCP.
 */
class SshScpHostFile extends SshHostFile implements HostFile {

	/**
	 * Constructs a SshScpHostFile
	 * 
	 * @param session
	 *            the session connected to the host
	 * @param remotePath
	 *            the path of the file on the host
	 */
	public SshScpHostFile(SshHostSession session, String remotePath) {
		super(session, remotePath);
	}

	public boolean exists() throws RuntimeIOException {
		return executeStat().exists;
	}

	public boolean isDirectory() throws RuntimeIOException {
		return executeStat().isDirectory;
	}

	public long length() throws RuntimeIOException {
		return executeStat().length;
	}

	public boolean canRead() throws RuntimeIOException {
		return executeStat().canRead;
	}

	public boolean canWrite() throws RuntimeIOException {
		return executeStat().canWrite;
	}

	public boolean canExecute() throws RuntimeIOException {
		return executeStat().canExecute;
	}

	public List<String> list() throws RuntimeIOException {
		CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
		// yes, this *is* meant to be 'el es min one'!
		int errno = executeCommand(capturedOutput, "ls", "-1", remotePath);
		if (errno != 0) {
			throw new RuntimeIOException("Cannot list directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}

		if (logger.isDebugEnabled())
			logger.debug("Listed directory " + this);
		return capturedOutput.getOutputLines();
	}

	public void mkdir() throws RuntimeIOException {
		mkdir(new String[0]);
	}

	public void mkdirs() throws RuntimeIOException {
		mkdir(new String[] { "-p" });
	}

	protected void mkdir(String[] args) throws RuntimeIOException {
		CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
		int errno = executeCommand(capturedOutput, makeMkdirCommand(args));
		if (errno != 0) {
			throw new RuntimeIOException("Cannot create directory or -ies " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Created directory " + this);
		}
	}

	private String[] makeMkdirCommand(String[] args) {
		int numArgs = args.length;
		String[] command = new String[numArgs + 2];
		command[0] = "mkdir";
		System.arraycopy(args, 0, command, 1, numArgs);
		command[numArgs + 1] = remotePath;
		return command;
	}

	public void moveTo(HostFile destFile) {
		if (destFile instanceof SshScpHostFile) {
			SshScpHostFile sshScpDestFile = (SshScpHostFile) destFile;
			if (sshScpDestFile.getSession() == getSession()) {
				CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
				int errno = executeCommand(capturedOutput, "mv", remotePath, sshScpDestFile.getPath());
				if (errno != 0) {
					throw new RuntimeIOException("Cannot move/rename file/directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
				}
			} else {
				throw new RuntimeIOException("Cannot move/rename SSH/SCP file/directory " + this + " to SSH/SCP file/directory " + destFile
						+ " because it is in a different session");
			}
		} else {
			throw new RuntimeIOException("Cannot move/rename SSH/SCP file/directory " + this + " to non-SSH/SCP file/directory " + destFile);
		}
	}

	@Override
	protected void deleteDirectory() {
		CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
		int errno = executeCommand(capturedOutput, "rmdir", remotePath);
		if (errno != 0) {
			throw new RuntimeIOException("Cannot delete directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}
		if (logger.isDebugEnabled())
			logger.debug("Deleted directory " + this);
	}

	@Override
	protected void deleteFile() {
		CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
		int errno = executeCommand(capturedOutput, "rm", "-f", remotePath);
		if (errno != 0) {
			throw new RuntimeIOException("Cannot delete file " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}
		if (logger.isDebugEnabled())
			logger.debug("Deleted file " + this);
	}

	@Override
	public boolean deleteRecursively() throws RuntimeIOException {
		if (!exists()) {
			return false;
		} else {
			CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
			int errno = executeCommand(capturedOutput, "rm", "-rf", remotePath);
			if (errno != 0) {
				throw new RuntimeIOException("Cannot recursively delete directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
			}
			if (logger.isDebugEnabled())
				logger.debug("Recursively deleted file/directory " + this);
			return true;
		}
	}

	public InputStream get() throws RuntimeIOException {
		SshScpInputStream in = new SshScpInputStream(this);
		in.open();
		if (logger.isDebugEnabled())
			logger.debug("Opened SCP input stream from file " + this);
		return in;
	}

	public OutputStream put(long length) throws RuntimeIOException {
		SshScpOutputStream out = new SshScpOutputStream(this, length);
		out.open();
		if (logger.isDebugEnabled())
			logger.debug("Opened SCP output stream to file " + this);
		return out;
	}

	private Logger logger = LoggerFactory.getLogger(SshScpHostFile.class);

}
