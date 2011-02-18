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

package com.xebialabs.overthere.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CommandExecutionCallbackHandler;

/**
 * Checks the output of an executed command and gives canned responses to expected output.
 */
public class InputResponseHandler implements CommandExecutionCallbackHandler {

	private CommandExecutionCallbackHandler decoratedHandler;
	private OutputStream remoteStdin;
	private Map<String, String> inputResponse;
	private StringBuffer receivedOutputBuffer = new StringBuffer();

	public InputResponseHandler(CommandExecutionCallbackHandler decoratedHandler, OutputStream remoteStdin, Map<String, String> inputResponse) {
		this.decoratedHandler = checkNotNull(decoratedHandler, "CommandExecutionCallbackHandler is null");
		this.remoteStdin = checkNotNull(remoteStdin, "OutputStream is null");
		this.inputResponse = checkNotNull(inputResponse, "Map<String, String> is null");
	}

	public void handleOutput(char c) {
		decoratedHandler.handleOutput(c);
		receivedOutputBuffer.append(c);

		String receivedOutput = receivedOutputBuffer.toString();
		for (String key : inputResponse.keySet()) {
			if (receivedOutput.endsWith(key)) {
				String response = inputResponse.get(key);
				if (logger.isDebugEnabled()) {
					logger.debug("Found prompt in output: " + receivedOutput);
				}
				try {
					remoteStdin.write(response.getBytes());
					remoteStdin.flush();
					if (logger.isDebugEnabled()) {
						logger.debug("Sent response " + response);
					}
				} catch (IOException exc) {
					logger.error("Cannot send response " + response, exc);
				}
			}
		}
	}

	public void handleOutputLine(String line) {
		decoratedHandler.handleOutputLine(line);
		receivedOutputBuffer = new StringBuffer();
	}

	public void handleErrorLine(String line) {
		decoratedHandler.handleErrorLine(line);
	}

	private static Logger logger = LoggerFactory.getLogger(InputResponseHandler.class.getName());

}
