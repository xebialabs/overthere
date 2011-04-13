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
		if (in == null)
			throw new NullPointerException("InputStream is null");
		if (handler == null)
			throw new NullPointerException("CommandExecutionCallbackHandler is null");

		this.in = in;
		this.handler = handler;
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
