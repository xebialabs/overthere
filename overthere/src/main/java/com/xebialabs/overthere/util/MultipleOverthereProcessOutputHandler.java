/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2011 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.util;

import com.xebialabs.overthere.OverthereProcessOutputHandler;

/**
 * Implementation of the {@link OverthereProcessOutputHandler} interface that sends the output to one or more other {@link OverthereProcessOutputHandler
 * handlers}.
 */
public class MultipleOverthereProcessOutputHandler implements OverthereProcessOutputHandler {

	private final OverthereProcessOutputHandler[] handlers;

	private MultipleOverthereProcessOutputHandler(final OverthereProcessOutputHandler... handlers) {
		this.handlers = handlers;
	}

	@Override
	public void handleOutputLine(final String line) {
		for (OverthereProcessOutputHandler h : handlers) {
			h.handleOutputLine(line);
		}
	}

	@Override
	public void handleErrorLine(final String line) {
		for (OverthereProcessOutputHandler h : handlers) {
			h.handleErrorLine(line);
		}
	}

	@Override
	public void handleOutput(final char c) {
		for (OverthereProcessOutputHandler h : handlers) {
			h.handleOutput(c);
		}
	}

	/**
	 * Creates a {@link MultipleOverthereProcessOutputHandler}.
	 * 
	 * @param handlers
	 *            the handlers where the output should be sent to.
	 * @return the created {@link MultipleOverthereProcessOutputHandler}.
	 */
	public static MultipleOverthereProcessOutputHandler multiHandler(final OverthereProcessOutputHandler... handlers) {
		return new MultipleOverthereProcessOutputHandler(handlers);
	}

}

