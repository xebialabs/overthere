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
    WINDOWS('\\', ';', "\r\n", "&&", ".bat", "C:\\windows\\temp", "UTF-8"),

    /**
     * An operating system from the Unix family: Linux, AIX, MacOS, etc.
     */
    UNIX('/', ':', "\n", ";", ".sh", "/tmp", "UTF-8"),

    /**
     * The Z/OS operating system.
     */
    ZOS('/', ':', "\n", ";", ".sh", "/tmp", "Cp1047");

    private final String fileSeparator;

    private final char fileSeparatorChar;

    private final String pathSeparator;

    private final char pathSeparatorChar;

    private final String lineSeparator;

    private final String commandSeparator;

    private final String scriptExtension;

    private final String defaultTemporaryDirectoryPath;

    private final String defaultCharacterSet;

    private OperatingSystemFamily(char fileSeparatorChar, char pathSeparatorChar, String lineSeparator, String commandSepator, String scriptExtension,
                                  String defaultTemporaryDirectoryPath, String defaultCharacterSet) {
        this.scriptExtension = scriptExtension;
        this.lineSeparator = lineSeparator;
        this.defaultTemporaryDirectoryPath = defaultTemporaryDirectoryPath;
        this.fileSeparator = String.valueOf(fileSeparatorChar);
        this.fileSeparatorChar = fileSeparatorChar;
        this.pathSeparator = String.valueOf(pathSeparatorChar);
        this.pathSeparatorChar = pathSeparatorChar;
        this.commandSeparator = commandSepator;
        this.defaultCharacterSet = defaultCharacterSet;
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
     * @param text the text to convert.
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
     * Returns the string used by the operating system family to separate components of a file path, e.g. <tt>\</tt> or
     * <tt>/<tt>.
     *
     * @return the file separator.
     */
    public String getFileSeparator() {
        return fileSeparator;
    }

    /**
     * Returns the character used by the operating system family to separate components of a file path, e.g. <tt>\</tt>
     * or <tt>/<tt>.
     *
     * @return the file separator.
     */
    public char getFileSeparatorChar() {
        return fileSeparatorChar;
    }

    /**
     * Returns the string used by the operating system family to separate components of a path, e.g. <tt>;</tt> or
     * <tt>:<tt>.
     *
     * @return the file separator.
     */
    public String getPathSeparator() {
        return pathSeparator;
    }

    /**
     * Returns the character used by the operating system family to separate components of a path, e.g. <tt>;</tt> or
     * <tt>:<tt>.
     *
     * @return the file separator.
     */
    public char getPathSeparatorChar() {
        return pathSeparatorChar;
    }

    /**
     * Returns the characters used by the operating system family to separate line in a text file, e.g. <tt>\r\n</tt> or
     * <tt>\n</tt>
     *
     * @return the line separator
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Returns the string used by the operating system family to separate commands in a command line, e.g. <tt>&&</tt>
     * or <tt>;<tt>.
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
     * Returns the default path of the temporary directory for this operating system family, i.e. <tt>C:\temp</tt> or
     * <tt>/tmp</tt>.
     *
     * @return the path
     */
    public String getDefaultTemporaryDirectoryPath() {
        return defaultTemporaryDirectoryPath;
    }

    /**
     * Returns the default character set encoding for this operating system family, i.e. <tt>UTF-8</tt> for Windows and Unix, or
     * <tt>Cp1047</tt> for Z/OS.
     *
     * @return the character set name
     */
    public String getDefaultCharacterSet() {
        return defaultCharacterSet;
    }
}
