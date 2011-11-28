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

package com.xebialabs.overthere;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * The family (flavour) of the operating system running on a host.
 */
public enum OperatingSystemFamily {
	/**
	 * An operating system from the Windows family: NT, XP, Server 2003, Vista, etc.
	 */
	WINDOWS,

	/**
	 * An operating system from the Unix family: Linux, AIX, MacOS, etc.
	 */
	UNIX;

	/**
	 * Returns the {@link OperatingSystemFamily} that corresponds to the local host
	 */
	public static OperatingSystemFamily getLocalHostOperatingSystemFamily() {
		return System.getProperty("os.name").startsWith("Windows") ? WINDOWS : UNIX;
	}

	/**
	 * Returns the extension for scripts used by the operating system family, e.g. <tt>.bat</tt> or <tt>.sh</tt>
	 * 
	 * 
	 * @return the script extension including the preceding dot
	 */
	public String getScriptExtension() {
		if (this == WINDOWS) {
			return ".bat";
		} else {
			return ".sh";
		}
	}

	/**
	 * Returns the characters used by the operating system family to separate line in a text file, e.g. <tt>\r\n</tt> or <tt>\n</tt>
	 * 
	 * @return the line separator
	 */
	public String getLineSeparator() {
		if (this == WINDOWS) {
			return "\r\n";
		} else {
			return "\n";
		}
	}

	/**
	 * Returns the default path of the temporary directory for this operating system family, i.e. <tt>C:\temp</tt> or <tt>/tmp</tt>.
	 * 
	 * @return the path
	 */
	public String getDefaultTemporaryDirectoryPath() {
		if (this == WINDOWS) {
			return "C:\\windows\\temp";
		} else {
			return "/tmp";
		}
	}

	/**
	 * Returns the character used by the operating system family to separate components of a file path, e.g. <tt>\</tt> or <tt>/<tt>.
	 * 
	 * @return the file separator.
	 */
	public String getFileSeparator() {
		if (this == WINDOWS) {
			return "\\";
		} else {
			return "/";
		}
	}

	/**
	 * Returns the character used by the operating system family to separate components of a path, e.g. <tt>;</tt> or <tt>:<tt>.
	 * 
	 * @return the file separator.
	 */
	public String getPathSeparator() {
		if (this == WINDOWS) {
			return ";";
		} else {
			return ":";
		}
	}

	/**
	 * Returns the character used by the operating system family to separate commands in a command line, e.g. <tt>&&</tt> or <tt>;<tt>.
	 * 
	 * @return the command separator.
	 */
	public String getCommandSeparator() {
		if (this == WINDOWS) {
			return "&&";
		} else {
			return ";";
		}
	}

	/**
	 * Converts a text to use the {@link #getLineSeparator() line separator} of this operating system family.
	 * 
	 * @param text
	 *            the text to convert.
	 * @return the converted text.
	 */
	public String convertText(String text) {
		if (text == null) {
			return null;
		}

		String lineSep = getLineSeparator();
		try {
			StringBuilder converted = new StringBuilder();
			BufferedReader r = new BufferedReader(new StringReader(text));
			String line;
			while ((line = r.readLine()) != null) {
				converted.append(line).append(lineSep);
			}
			return converted.toString();
		} catch (IOException exc) {
			throw new RuntimeIOException("Unable to read String", exc);
		}
	}

}

