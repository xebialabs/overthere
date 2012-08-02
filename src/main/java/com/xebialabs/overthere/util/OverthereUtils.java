/**
 * Copyright (c) 2008, 2012, XebiaLabs B.V., All rights reserved.
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
package com.xebialabs.overthere.util;

import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

import java.io.UnsupportedEncodingException;

/**
 * Contains a number of static helper methods.
 */
public class OverthereUtils {

    /**
     * Writes the contents of a byte array to an {@link OverthereFile}.
     *
     * @param from
     *            the byte array to copy from.
     * @param to
     *            the file to copy to.
     */
    public static void write(final byte[] from, final OverthereFile to) {
        new ByteArrayFile(to.getPath(), from).copyTo(to);
    }

    /**
     * Writes the contents of a {@link String} to an {@link OverthereFile}.
     *
     * @param from
     *            the string to copy from.
     * @param encoding
     *            the {@link String#getBytes(String) encoding} to use.
     * @param to
     *            the file to copy to.
     */
    public static void write(final String from, final String encoding, final OverthereFile to) {
        try {
            write(from.getBytes(encoding), to);
        } catch (UnsupportedEncodingException exc) {
            throw new RuntimeIOException("Cannot write string to " + to, exc);
        }
    }

    /**
     * Returns the name component of a path. The name component is the part after the last slash or backslash.
     *
     * @param path
     *            the path
     * @return the name component.
     */
    public static String getName(String path) {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0) {
            return path.substring(lastSlash + 1);
        }

        int lastBackslash = path.lastIndexOf('\\');
        if (lastBackslash >= 0) {
            return path.substring(lastBackslash + 1);
        }

        return path;
    }

    /**
     * Returns the extension of a name (not a path). The extension is the part after the last dot.
     *
     * @param name
     *            the name
     * @return the extension.
     */
    public static String getBaseName(String name) {
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            return name.substring(0, dot);
        }
        return name;
    }

    /**
     * Returns the base name of a name (not a path). The base name is the part before the last dot.
     *
     * @param name
     *            the name
     * @return the base name.
     */
    public static String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            return name.substring(dot + 1);
        }
        return name;
    }

}
