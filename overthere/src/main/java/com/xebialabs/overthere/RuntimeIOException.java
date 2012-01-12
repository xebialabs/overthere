/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
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

package com.xebialabs.overthere;

/**
 * Thrown whenever an I/O error occurs.
 */
@SuppressWarnings("serial")
public class RuntimeIOException extends RuntimeException {

	/**
	 * Constructs an <code>RuntimeIOException</code> with <code>null</code> as its detail message.
	 */
	public RuntimeIOException() {
		super();
	}

	/**
	 * Constructs an <code>RuntimeIOException</code> with the specified detail message.
	 * 
	 * @param message
	 *            the detail message.
	 */
	public RuntimeIOException(String message) {
		super(message);
	}

	/**
	 * Constructs an <code>RuntimeIOException</code> with specified cause and a detail message of (cause==null ? null : cause.toString()) (which typically
	 * contains the class and detail message of cause).
	 * 
	 * @param cause
	 *            the root cause
	 */
	public RuntimeIOException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs an <code>RuntimeIOException</code> with the specified detail message and cause.
	 * 
	 * @param message
	 *            the detail message.
	 * @param cause
	 *            the root cause
	 */
	public RuntimeIOException(String message, Throwable cause) {
		super(message, cause);
	}

}

