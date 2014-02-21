/*
 * Copyright (c) 2008-2014, XebiaLabs B.V., All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * Contains a number of static helper methods.
 */
public class OverthereUtils {

    /**
     * Reads the contents of an {@link OverthereFile} into a byte array.
     *
     * @param from the file to read from.
     * @returns the byte array.
     */
    public static byte[] read(final OverthereFile from) {
        try {
            return ByteStreams.toByteArray(new InputSupplier<InputStream>() {
                @Override
                public InputStream getInput() throws IOException {
                    return from.getInputStream();
                }
            });
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * Reads the contents of an {@link OverthereFile} into a string.
     *
     * @param from        the file to read from.
     * @param charsetName the {@link java.nio.charset.Charset charset} to use.
     * @returns the string.
     */
    public static String read(final OverthereFile from, final String charsetName) {
        try {
            return CharStreams.toString(new InputSupplier<Reader>() {
                @Override
                public Reader getInput() throws IOException {
                    return new InputStreamReader(from.getInputStream(), charsetName);
                }
            });
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * Writes the contents of a byte array to an {@link OverthereFile}.
     *
     * @param from the byte array to copy from.
     * @param to   the file to write to.
     */
    public static void write(final byte[] from, final OverthereFile to) {
        new ByteArrayFile(to.getPath(), from).copyTo(to);
    }

    /**
     * Writes the contents of a {@link String} to an {@link OverthereFile}.
     *
     * @param from        the string to copy from.
     * @param charsetName the {@link java.nio.charset.Charset charset} to use.
     * @param to          the file to write to.
     */
    public static void write(final String from, final String charsetName, final OverthereFile to) {
        try {
            write(from.getBytes(charsetName), to);
        } catch (UnsupportedEncodingException exc) {
            throw new RuntimeIOException("Cannot write string to " + to, exc);
        }
    }

    /**
     * Returns the name component of a path. The name component is the part after the last slash or backslash.
     *
     * @param path the path
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
     * Returns the base name of a name (not a path). The base name is the part before the last dot.
     *
     * @param name the name
     * @return the base name.
     */
    public static String getBaseName(String name) {
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            return name.substring(0, dot);
        }
        return name;
    }

    /**
     * Returns the extension of a name (not a path). The extension is the part from the last dot in a name onwards.
     * If there is no dot character in the name, this will return an empty string.
     *
     * @param name the name
     * @return the extension.
     */
    public static String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            return name.substring(dot);
        }
        return "";
    }

    /**
     * Construct a new (host) path from a parent directory, and a child.
     *
     * @param parent The parent directory
     * @param child  The path that should be appended to the parent.
     * @return A newly constructed path.
     */
    public static String constructPath(final OverthereFile parent, final String child) {
        return parent.getPath() + parent.getConnection().getHostOperatingSystem().getFileSeparator() + child;
    }

}
