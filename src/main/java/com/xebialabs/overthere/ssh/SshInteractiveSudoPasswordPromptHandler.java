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

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;

import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import org.slf4j.LoggerFactory;

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

