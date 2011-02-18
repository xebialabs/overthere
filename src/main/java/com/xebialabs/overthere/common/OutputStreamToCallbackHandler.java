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

import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CommandExecutionCallbackHandler;

/**
 * Runnable object that reads an {@link InputStream} an invokes a {@link CommandExecutionCallbackHandler} for every line read.
 */
public class OutputStreamToCallbackHandler implements Runnable {

	private InputStream in;

	private CommandExecutionCallbackHandler handler;

	public OutputStreamToCallbackHandler(InputStream in, CommandExecutionCallbackHandler handler) {
		this.in = checkNotNull(in, "InputStream is null");
		this.handler = checkNotNull(handler, "CommandExecutionCallbackHandler is null");
	}

	public void run() {

		InputStreamReader reader = new InputStreamReader(in);
		try {
			int readInt = reader.read();
			StringBuffer lineBuffer = new StringBuffer();
			while (readInt > -1) {
				char c = (char) readInt;
				handler.handleOutput(c);
				if (c != '\r' && c != '\n') {
					// add any character but a CR or LF to the line buffer
					lineBuffer.append(c);
				}
				if (c == '\n') {
					handler.handleOutputLine(lineBuffer.toString());
					lineBuffer = new StringBuffer();
				}
				readInt = reader.read();
			}
		} catch (Exception e) {
			logger.error("An exception occured", e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
	}

	private static Logger logger = LoggerFactory.getLogger(OutputStreamToCallbackHandler.class);

}