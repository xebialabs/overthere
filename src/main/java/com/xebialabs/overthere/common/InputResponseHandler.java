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
