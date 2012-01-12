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
	WINDOWS('\\', ';', "\r\n", "&&", ".bat", "C:\\windows\\temp"),

	/**
	 * An operating system from the Unix family: Linux, AIX, MacOS, etc.
	 */
	UNIX('/', ':', "\n", ";", ".sh", "/tmp");

	private final String fileSeparator;
	
	private final char fileSeparatorChar;

	private final String pathSeparator;

	private final char pathSeparatorChar;

	private final String lineSeparator;

	private final String commandSeparator;

	private final String scriptExtension;

	private final String defaultTemporaryDirectoryPath;

	private OperatingSystemFamily(char fileSeparatorChar, char pathSeparatorChar, String lineSeparator, String commandSepator, String scriptExtension, String defaultTemporaryDirectoryPath) {
		this.scriptExtension = scriptExtension;
		this.lineSeparator = lineSeparator;
		this.defaultTemporaryDirectoryPath = defaultTemporaryDirectoryPath;
		this.fileSeparator = String.valueOf(fileSeparatorChar);
		this.fileSeparatorChar = fileSeparatorChar;
		this.pathSeparator = String.valueOf(pathSeparatorChar);
		this.pathSeparatorChar = pathSeparatorChar;
		this.commandSeparator = commandSepator;
	}

	/**
	 * Returns the {@link OperatingSystemFamily} that corresponds to the local host
	 */
	public static OperatingSystemFamily getLocalHostOperatingSystemFamily() {
		return System.getProperty("os.name").startsWith("Windows") ? WINDOWS : UNIX;
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

	/**
	 * Returns the string used by the operating system family to separate components of a file path, e.g. <tt>\</tt> or <tt>/<tt>.
	 * 
	 * @return the file separator.
	 */
	public String getFileSeparator() {
		return fileSeparator;
	}

	/**
	 * Returns the character used by the operating system family to separate components of a file path, e.g. <tt>\</tt> or <tt>/<tt>.
	 * 
	 * @return the file separator.
	 */
	public char getFileSeparatorChar() {
		return fileSeparatorChar;
	}

	/**
	 * Returns the string used by the operating system family to separate components of a path, e.g. <tt>;</tt> or <tt>:<tt>.
	 * 
	 * @return the file separator.
	 */
	public String getPathSeparator() {
		return pathSeparator;
	}

	/**
	 * Returns the character used by the operating system family to separate components of a path, e.g. <tt>;</tt> or <tt>:<tt>.
	 * 
	 * @return the file separator.
	 */
	public char getPathSeparatorChar() {
		return pathSeparatorChar;
	}

	/**
	 * Returns the characters used by the operating system family to separate line in a text file, e.g. <tt>\r\n</tt> or <tt>\n</tt>
	 * 
	 * @return the line separator
	 */
	public String getLineSeparator() {
		return lineSeparator;
	}

	/**
	 * Returns the string used by the operating system family to separate commands in a command line, e.g. <tt>&&</tt> or <tt>;<tt>.
	 * 
	 * @return the command separator.
	 */
	public String getCommandSeparator() {
		return commandSeparator;
	}

	/**
	 * Returns the extension for scripts used by the operating system family, e.g. <tt>.bat</tt> or <tt>.sh</tt>
	 * 
	 * @return the script extension including the preceding dot
	 */
	public String getScriptExtension() {
		return scriptExtension;
	}

	/**
	 * Returns the default path of the temporary directory for this operating system family, i.e. <tt>C:\temp</tt> or <tt>/tmp</tt>.
	 * 
	 * @return the path
	 */
	public String getDefaultTemporaryDirectoryPath() {
		return defaultTemporaryDirectoryPath;
	}

}

