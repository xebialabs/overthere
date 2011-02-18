package com.xebialabs.overthere.ssh;

import java.io.InputStream;
import java.io.OutputStream;

import com.xebialabs.overthere.RuntimeIOException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import com.xebialabs.overthere.CapturingCommandExecutionCallbackHandler;
import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;
import org.slf4j.LoggerFactory;

/**
 * A file on a host connected through SSH w/ SUDO.
 */
class SshSudoHostFile extends SshScpHostFile implements HostFile {

	private SshSudoHostConnection sshSudoHostSession;

	private boolean isTempFile;

	/**
	 * Constructs a SshSudoHostFile
	 * 
	 * @param session
	 *            the connection connected to the host
	 * @param remotePath
	 *            the path of the file on the host
	 * @param isTempFile
	 *            is <code>true</code> if this is a temporary file; <code>false</code> otherwise
	 */
	public SshSudoHostFile(SshSudoHostConnection session, String remotePath, boolean isTempFile) {
		super(session, remotePath);
		this.sshSudoHostSession = session;
		this.isTempFile = isTempFile;
	}

	@Override
	protected int executeCommand(CommandExecutionCallbackHandler handler, String... command) {
		if (isTempFile) {
			return sshSudoHostSession.noSudoExecute(handler, command);
		} else {
			return super.executeCommand(handler, command);
		}
	}

	@Override
	public HostFile getFile(String name) {
		SshSudoHostFile f = (SshSudoHostFile) super.getFile(name);
		f.isTempFile = this.isTempFile;
		return f;
	}

	@Override
	public HostFile getParentFile() {
		SshSudoHostFile f = (SshSudoHostFile) super.getParentFile();
		f.isTempFile = this.isTempFile;
		return f;
	}

	@Override
	public InputStream get() throws RuntimeIOException {
		if (isTempFile) {
			return super.get();
		} else {
			HostFile tempFile = getTempFile(true);
			copyHostFileToTempFile(tempFile);
			return tempFile.get();
		}
	}

	private void copyHostFileToTempFile(HostFile tempFile) {
		if (logger.isDebugEnabled())
			logger.debug("Copying " + this + " to " + tempFile + " for reading");
		CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
		int result = connection.execute(capturedOutput, "cp", this.getPath(), tempFile.getPath());
		if (result != 0) {
			String errorMessage = capturedOutput.getAll();
			throw new RuntimeIOException("Cannot copy " + this + " to " + tempFile + " for reading: " + errorMessage);
		}
	}

	@Override
	public OutputStream put(long length) throws RuntimeIOException {
		if (isTempFile) {
			// XXX: this should really be returning a "ChmoddingSudoOutputStream"
			return super.put(length);
		} else {
			/*
			 * XXX: this should really be returning a "ChmoddingSudoOutputStream" wrapped in a "CopyOnCompletionOutputStream"
			 */
			SshSudoOutputStream out = new SshSudoOutputStream(this, length, getTempFile(false));
			out.open();
			if (logger.isDebugEnabled())
				logger.debug("Opened SUDO output stream to remote file " + this);
			return out;
		}
	}

	protected HostFile getTempFile(boolean useSudoForDeletion) {
		String prefix = FilenameUtils.getBaseName(getPath());
		String suffix = FilenameUtils.getExtension(getPath());
		return sshSudoHostSession.getTempFile(prefix, suffix);
	}

	@Override
	public void mkdir() throws RuntimeIOException {
		if (!isTempFile) {
			super.mkdir();
		} else {
			/*
			 * For SUDO access, temporary dirs also need to be writable to the connecting user, otherwise an SCP copy will fail. 1777 is world writable with the
			 * sticky bit set.
			 */
			logger.debug("Making directory world-writable (with sticky bit)");
			mkdir(new String[] { "-m", "1777" });
		}
	}

	private Logger logger = LoggerFactory.getLogger(SshSudoHostFile.class);

}
