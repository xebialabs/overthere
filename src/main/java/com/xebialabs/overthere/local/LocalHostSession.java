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

package com.xebialabs.overthere.local;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * A session to the local host.
 */
public class LocalHostSession extends AbstractHostSession implements HostSession {

	/**
	 * Constructs a session to the local host.
	 */
	public LocalHostSession(LocalHostSessionSpecification spec) {
		super(spec);
	}

	public HostFile getTempFile(String prefix, String suffix) throws RuntimeIOException {
		try {
			return new LocalHostFile(this, File.createTempFile(prefix, suffix, new File(getTemporaryDirectory().getPath())));
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	public HostFile getFile(String hostPath) throws RuntimeIOException {
		return new LocalHostFile(this, new File(hostPath));
	}

	public HostFile getFile(HostFile parent, String child) throws RuntimeIOException {
		if (!(parent instanceof LocalHostFile)) {
			throw new IllegalStateException("parent is not a file on the local host");
		}
		File parentFile = ((LocalHostFile) parent).getFile();
		return new LocalHostFile(this, new File(parentFile, child));
	}

	@SuppressWarnings("unchecked")
	public int execute(CommandExecutionCallbackHandler handler, String... cmdarray) throws RuntimeIOException {
		return execute(handler, Collections.EMPTY_MAP, cmdarray);
	}

	public int execute(CommandExecutionCallbackHandler handler, Map<String, String> inputResponse, String... cmdarray) {
		String commandLineForLogging = encodeCommandLineForLogging(cmdarray);

		Process proc;
		try {
			if (getHostOperatingSystem() == OperatingSystemFamily.WINDOWS) {
				logger.debug("Enabling Windows specific command line encoding");
				logger.info("Executing local command: " + commandLineForLogging);
				proc = Runtime.getRuntime().exec(encodeCommandLineForExecution(cmdarray));
			} else {
				logger.info("Executing local command: " + commandLineForLogging);
				proc = Runtime.getRuntime().exec(cmdarray);
			}
		} catch (IOException exc) {
			throw new RuntimeIOException("Could not start local command: " + commandLineForLogging, exc);
		}

		InputResponseHandler responseHandler = new InputResponseHandler(handler, proc.getOutputStream(), inputResponse);
		Thread outputGobblerThread = new Thread(new OutputStreamToCallbackHandler(proc.getInputStream(), responseHandler));
		outputGobblerThread.start();

		Thread errorGobblerThread = new Thread(new ErrorStreamToCallbackHandler(proc.getErrorStream(), responseHandler));
		errorGobblerThread.start();

		try {
			outputGobblerThread.join();
			errorGobblerThread.join();
			return proc.waitFor();
		} catch (InterruptedException exc) {
			Thread.currentThread().interrupt();
			throw new AbortedException("Local command was interrupted: " + commandLineForLogging, exc);
		}
	}

	public CommandExecution startExecute(String... commandLine) {
		return null;
	}

	public String toString() {
		return "localhost";
	}

	private static Logger logger = LoggerFactory.getLogger(LocalHostSession.class);

}
