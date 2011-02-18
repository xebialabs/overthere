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

import com.xebialabs.overthere.CommandExecutionCallbackHandler;

/**
 * Decorates a {@code CommandExecutionCallbackHandler}, handling SUDO password prompts
 * transparently and removing them from the output passed to the decorated handler.
 */
public class SshInteractiveSudoPasswordPromptHandler implements CommandExecutionCallbackHandler {

	private CommandExecutionCallbackHandler decoratedHandler;
	private OutputStream remoteStdin;
	private byte[] passwordBytes;

	private StringBuffer receivedOutputBuffer = new StringBuffer();
	private boolean onFirstLine = true;
	private boolean justSawPasswordPrompt = false;

	public SshInteractiveSudoPasswordPromptHandler(CommandExecutionCallbackHandler decoratedHandler, OutputStream remoteStdin, String password) {
		this.decoratedHandler = decoratedHandler;
		this.remoteStdin = remoteStdin;
		this.passwordBytes = (password + "\r\n").getBytes();
	}

	public void handleOutput(char c) {
		decoratedHandler.handleOutput(c);
		if (onFirstLine) {
			receivedOutputBuffer.append(c);
			String receivedOutput = receivedOutputBuffer.toString();
			if (c == ':' && receivedOutput.contains("assword")) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found password prompt in first line of output: " + receivedOutput);
				}
				try {
					remoteStdin.write(passwordBytes);
					remoteStdin.flush();
					if (logger.isDebugEnabled()) {
						logger.debug("Sent password");
					}
				} catch (IOException exc) {
					logger.error("Cannot send password", exc);
				}
				justSawPasswordPrompt = true;
			}
		}
	}

	public void handleOutputLine(String line) {
		if (!justSawPasswordPrompt) {
			decoratedHandler.handleOutputLine(line);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Not sending line with password prompt to decorated handler: " + line);
			}
			justSawPasswordPrompt = false;
		}
		onFirstLine = false;
	}

	public void handleErrorLine(String line) {
		decoratedHandler.handleErrorLine(line);
		onFirstLine = false;
	}

	private static Logger logger = LoggerFactory.getLogger(SshInteractiveSudoPasswordPromptHandler.class);

}
