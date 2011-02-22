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
package com.xebialabs.overthere.common;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.LineIterator;

import com.xebialabs.overthere.CommandExecutionCallbackHandler;

/**
 * Runnable object that reads an {@link InputStream} an invokes a {@link CommandExecutionCallbackHandler} for every line read.
 */
public class ErrorStreamToCallbackHandler implements Runnable {

	private InputStream in;

	private CommandExecutionCallbackHandler handler;

	public ErrorStreamToCallbackHandler(InputStream in, CommandExecutionCallbackHandler handler) {
		this.in = in;
		this.handler = handler;
	}

	public void run() {
		if (handler == null) {
			return;
		}

		LineIterator lines = new LineIterator(new InputStreamReader(in));
		try {
			while (lines.hasNext()) {
				String line = lines.nextLine();
				handler.handleErrorLine(line);
			}
		} finally {
			LineIterator.closeQuietly(lines);
		}
	}

}

