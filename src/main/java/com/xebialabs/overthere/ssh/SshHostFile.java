package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.common.AbstractHostFile;

/**
 * A file on a host connected through SSH.
 */
abstract class SshHostFile extends AbstractHostFile implements HostFile {

	protected SshHostConnection sshHostSession;

	protected String remotePath;

	/**
	 * Constructs a SshHostFile
	 * 
	 * @param session
	 *            the connection connected to the host
	 * @param remotePath
	 *            the path of the file on the host
	 */
	SshHostFile(SshHostConnection session, String remotePath) {
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
