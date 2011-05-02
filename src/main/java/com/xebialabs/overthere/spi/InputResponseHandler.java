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
package com.xebialabs.overthere.spi;

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
		if (decoratedHandler == null)
			throw new NullPointerException("CommandExecutionCallbackHandler is null");
		if (remoteStdin == null)
			throw new NullPointerException("OutputStream is null");
		if (inputResponse == null)
			throw new NullPointerException("Map<String, String> is null");

		this.decoratedHandler = decoratedHandler;
		this.remoteStdin = remoteStdin;
		this.inputResponse = inputResponse;
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
