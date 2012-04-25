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

package com.xebialabs.overthere.cifs.winrm.exception;

import java.io.IOException;
import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.xebialabs.overthere.RuntimeIOException;

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

	private static String toString(Document doc) {
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

