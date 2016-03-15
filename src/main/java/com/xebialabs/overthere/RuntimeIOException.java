/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
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
     * @param message the detail message.
     */
    public RuntimeIOException(String message) {
        super(message);
    }

    /**
     * Constructs an <code>RuntimeIOException</code> with specified cause and a detail message of (cause==null ? null :
     * cause.toString()) (which typically contains the class and detail message of cause).
     *
     * @param cause the root cause
     */
    public RuntimeIOException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an <code>RuntimeIOException</code> with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the root cause
     */
    public RuntimeIOException(String message, Throwable cause) {
        super(message, cause);
    }

}
