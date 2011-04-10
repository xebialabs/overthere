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

import static com.xebialabs.overthere.common.OverthereHostConnectionUtils.getFileInfo;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CapturingCommandExecutionCallbackHandler;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * A file on a host connected through SSH w/ SCP.
 */
@SuppressWarnings("serial")
class SshScpOverthereFile extends SshOverthereFile {

	/**
	 * Constructs an SshScpOverthereFile
	 * 
	 * @param connection
	 *            the connection to the host
	 * @param remotePath
	 *            the path of the file on the host
	 */
	public SshScpOverthereFile(SshHostConnection connection, String remotePath) {
		super(connection, remotePath);
	}

	public boolean exists() {
		return getFileInfo(this).exists;
	}

	public boolean isDirectory() {
		return getFileInfo(this).isDirectory;
	}

	public long length() {
		return getFileInfo(this).length;
	}

	public boolean canRead() {
		return getFileInfo(this).canRead;
	}

	public boolean canWrite() {
		return getFileInfo(this).canWrite;
	}

	public boolean canExecute() {
		return getFileInfo(this).canExecute;
	}

	public String[] list() throws RuntimeIOException {
		CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
		// yes, this *is* meant to be 'el es min one'!
		int errno = executeCommand(capturedOutput, "ls", "-1", getPath());
		if (errno != 0) {
			throw new RuntimeIOException("Cannot list directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}

		if (logger.isDebugEnabled())
			logger.debug("Listed directory " + this);

		List<String> lsLines = capturedOutput.getOutputLines();
		return lsLines.toArray(new String[lsLines.size()]);
	}

	public boolean mkdir() {
		return mkdir(new String[0]);
	}

	public boolean mkdirs() {
		return mkdir(new String[] { "-p" });
	}

	protected boolean mkdir(String[] mkdirOptions) throws RuntimeIOException {
		String[] command = new String[mkdirOptions.length + 2];
		command[0] = "mkdir";
		for (int i = 0; i < mkdirOptions.length; i++) {
			command[i + 1] = mkdirOptions[i];
		}
		command[command.length - 1] = getPath();

		CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
		int errno = executeCommand(capturedOutput, command);
		if (errno != 0) {
			logger.error("Cannot create directory or -ies " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
			return false;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Created directory " + this);
		}
		return true;
	}

	@Override
	public boolean renameTo(File destFile) {
		if (destFile instanceof SshScpOverthereFile) {
			SshScpOverthereFile sshScpDestFile = (SshScpOverthereFile) destFile;
			if (sshScpDestFile.getConnection() == getConnection()) {
				CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
				int errno = executeCommand(capturedOutput, "mv", getPath(), sshScpDestFile.getPath());
				if (errno != 0) {
					throw new RuntimeIOException("Cannot move/rename file/directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
				}
				return true;
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
		int errno = executeCommand(capturedOutput, "rmdir", getPath());
		if (errno != 0) {
			throw new RuntimeIOException("Cannot delete directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}
		if (logger.isDebugEnabled())
			logger.debug("Deleted directory " + this);
	}

	@Override
	protected void deleteFile() {
		CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
		int errno = executeCommand(capturedOutput, "rm", "-f", getPath());
		if (errno != 0) {
			throw new RuntimeIOException("Cannot delete file " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}
		if (logger.isDebugEnabled())
			logger.debug("Deleted file " + this);
	}

	@Override
	public boolean deleteRecursively() throws RuntimeIOException {
		CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
		int errno = executeCommand(capturedOutput, "rm", "-rf", getPath());
		if (errno != 0) {
			throw new RuntimeIOException("Cannot recursively delete directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}
		if (logger.isDebugEnabled())
			logger.debug("Recursively deleted file/directory " + this);
		return true;
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

	private Logger logger = LoggerFactory.getLogger(SshScpOverthereFile.class);

}
