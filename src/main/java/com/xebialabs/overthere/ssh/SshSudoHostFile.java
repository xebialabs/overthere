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

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import com.xebialabs.deployit.exception.RuntimeIOException;
import com.xebialabs.overthere.CapturingCommandExecutionCallbackHandler;
import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;

/**
 * A file on a host connected through SSH w/ SUDO.
 */
class SshSudoHostFile extends SshScpHostFile implements HostFile {

	private SshSudoHostSession sshSudoHostSession;

	private boolean isTempFile;

	/**
	 * Constructs a SshSudoHostFile
	 * 
	 * @param session
	 *            the session connected to the host
	 * @param remotePath
	 *            the path of the file on the host
	 * @param isTempFile
	 *            is <code>true</code> if this is a temporary file; <code>false</code> otherwise
	 */
	public SshSudoHostFile(SshSudoHostSession session, String remotePath, boolean isTempFile) {
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
		int result = session.execute(capturedOutput, "cp", this.getPath(), tempFile.getPath());
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
