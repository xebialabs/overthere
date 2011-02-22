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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.xebialabs.overthere.RuntimeIOException;
import org.slf4j.Logger;

import com.xebialabs.overthere.CapturingCommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;
import org.slf4j.LoggerFactory;

/**
 * A file on a host connected through SSH w/ SCP.
 */
class SshScpHostFile extends SshHostFile implements HostFile {

	/**
	 * Constructs a SshScpHostFile
	 * 
	 * @param session
	 *            the connection connected to the host
	 * @param remotePath
	 *            the path of the file on the host
	 */
	public SshScpHostFile(SshHostConnection session, String remotePath) {
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
			if (sshScpDestFile.getConnection() == getConnection()) {
				CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
				int errno = executeCommand(capturedOutput, "mv", remotePath, sshScpDestFile.getPath());
				if (errno != 0) {
					throw new RuntimeIOException("Cannot move/rename file/directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
				}
			} else {
				throw new RuntimeIOException("Cannot move/rename SSH/SCP file/directory " + this + " to SSH/SCP file/directory " + destFile
						+ " because it is in a different connection");
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

