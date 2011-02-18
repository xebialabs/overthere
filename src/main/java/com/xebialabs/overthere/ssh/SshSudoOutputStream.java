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

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;

import com.xebialabs.deployit.exception.RuntimeIOException;
import com.xebialabs.overthere.CapturingCommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;

/**
 * An output stream to a file on a host connected through SSH w/ SUDO.
 */
class SshSudoOutputStream extends OutputStream {

	private HostFile tempFile;

	private SshSudoHostFile hostFile;

	private long length;

	private OutputStream tempFileOutputStream;

	public SshSudoOutputStream(SshSudoHostFile hostFile, long length, HostFile tempFile) {
		this.hostFile = hostFile;
		this.length = length;
		this.tempFile = tempFile;
	}

	void open() {
		tempFileOutputStream = tempFile.put(length);
	}

	@Override
	public void write(int b) throws IOException {
		tempFileOutputStream.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		tempFileOutputStream.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		tempFileOutputStream.write(b);
	}

	@Override
	public void close() throws IOException {
		tempFileOutputStream.close();
		copyTempFileToHostFile();
	}

	private void copyTempFileToHostFile() {
		if (logger.isDebugEnabled())
			logger.debug("Copying " + tempFile + " to " + hostFile + " after writing");
		CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
		int result = hostFile.getSession().execute(capturedOutput, "cp", tempFile.getPath(), hostFile.getPath());
		if (result != 0) {
			String errorMessage = capturedOutput.getAll();
			throw new RuntimeIOException("Cannot copy " + tempFile + " to " + hostFile + " after writing: " + errorMessage);
		}
	}

	private Logger logger = LoggerFactory.getLogger(SshSudoOutputStream.class);

}
