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

import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.common.AbstractHostFile;

/**
 * A file on a host connected through SSH.
 */
abstract class SshHostFile extends AbstractHostFile implements HostFile {

	protected SshHostSession sshHostSession;

	protected String remotePath;

	/**
	 * Constructs a SshHostFile
	 * 
	 * @param session
	 *            the session connected to the host
	 * @param remotePath
	 *            the path of the file on the host
	 */
	SshHostFile(SshHostSession session, String remotePath) {
		super(session);
		sshHostSession = session;
		this.remotePath = remotePath;
	}

	public String getPath() {
		return remotePath;
	}

	public String getName() {
		String fileSep = sshHostSession.getHostOperatingSystem().getFileSeparator();
		int lastFileSepPos = remotePath.lastIndexOf(fileSep);
		if (lastFileSepPos < 0) {
			return remotePath;
		} else {
			return remotePath.substring(lastFileSepPos + 1);
		}
	}

	public String getParent() {
		String fileSep = sshHostSession.getHostOperatingSystem().getFileSeparator();
		int lastFileSepPos = remotePath.lastIndexOf(fileSep);
		if (lastFileSepPos < 0 || remotePath.equals(fileSep)) {
			return "";
		} else if (lastFileSepPos == 0) {
			// the parent of something in the root directory is the root
			// directory itself.
			return fileSep;
		} else {
			return remotePath.substring(0, lastFileSepPos);
		}
	}

	public boolean delete() throws RuntimeIOException {
		if (!exists()) {
			return false;
		} else if (isDirectory()) {
			deleteDirectory();
			return true;
		} else {
			deleteFile();
			return true;
		}
	}

	protected int executeCommand(CommandExecutionCallbackHandler handler, String... command) {
		return sshHostSession.execute(handler, command);
	}

	protected abstract void deleteFile();

	protected abstract void deleteDirectory();

	public String toString() {
		return getPath() + " on " + sshHostSession;
	}

}
