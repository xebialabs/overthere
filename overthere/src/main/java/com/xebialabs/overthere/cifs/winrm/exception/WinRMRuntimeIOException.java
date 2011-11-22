/*
 * This file is part of WinRM.
 *
 * WinRM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WinRM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WinRM.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere.cifs.winrm.exception;

import com.xebialabs.overthere.RuntimeIOException;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;

@SuppressWarnings("serial")
public class WinRMRuntimeIOException extends RuntimeIOException {

	final Document in;
	final Document out;

	public WinRMRuntimeIOException(String message, Document in, Document out, Throwable cause) {
		super(message, cause);
		this.in = in;
		this.out = out;
	}

	public WinRMRuntimeIOException(String message) {
		this(message, null, null, null);

	}

	public WinRMRuntimeIOException(String message, Throwable throwable) {
		this(message, null, null, throwable);
	}

	@Override
	public String getMessage() {
		return String.format("%s, document in %s, document out %s,", super.getMessage(), toString(in), toString(out));
	}

	private String toString(Document doc) {
		if (doc == null) {
			return "[EMPTY]";
		}

		StringWriter stringWriter = new StringWriter();
		XMLWriter xmlWriter = new XMLWriter(stringWriter, OutputFormat.createPrettyPrint());
		try {
			xmlWriter.write(doc);
			xmlWriter.close();
		} catch (IOException e) {
			throw new RuntimeException("error ", e);
		}
		return stringWriter.toString();
	}

}
