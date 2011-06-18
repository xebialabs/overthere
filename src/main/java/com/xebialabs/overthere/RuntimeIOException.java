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
