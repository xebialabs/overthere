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
package com.xebialabs.overthere.util;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.CharBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_FILE_CREATION_RETRIES;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_FILE_CREATION_RETRIES_DEFAULT;
import static java.lang.String.format;

/**
 * Contains a number of static helper methods.
 */
public class OverthereUtils {

    private static final Logger logger = LoggerFactory.getLogger(OverthereUtils.class);
    /**
     * Reads the contents of an {@link OverthereFile} into a byte array.
     *
     * @param from the file to read from.
     * @return the byte array.
     */
    public static byte[] read(final OverthereFile from) {
        InputStream is = from.getInputStream();
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int nRead;
            byte[] bytes = new byte[1024];
            while ((nRead = is.read(bytes, 0, bytes.length)) != -1) {
                os.write(bytes, 0, nRead);
            }

            return os.toByteArray();
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        } finally {
            closeQuietly(is);
        }
    }

    /**
     * Reads the contents of an {@link OverthereFile} into a string.
     *
     * @param from        the file to read from.
     * @param charsetName the {@link java.nio.charset.Charset charset} to use.
     * @return the string.
     */
    public static String read(final OverthereFile from, final String charsetName) {
        InputStream is = from.getInputStream();
        try {
            InputStreamReader isr = new InputStreamReader(is, charsetName);
            StringBuilder b = new StringBuilder();
            int nRead;
            char[] chars = new char[1024];
            while ((nRead = isr.read(chars, 0, chars.length)) != -1) {
                b.append(chars, 0, nRead);
            }

            return b.toString();
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        } finally {
            closeQuietly(is);
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
     * Writes the contents of an {@link java.io.InputStream} to an {@link OverthereFile}.
     *
     * @param from the {@link java.io.InputStream} to read from.
     * @param to   the file to write to.
     */
    public static void write(final InputStream from, final OverthereFile to) {
        OutputStream toStream = to.getOutputStream();
        try {
            write(from, toStream);
        } finally {
            closeQuietly(toStream);
        }
    }

    /**
     * Writes the contents of an {@link java.io.InputStream} to an {@link java.io.OutputStream}.
     *
     * @param from the {@link java.io.InputStream} to read from.
     * @param to the {@link java.io.OutputStream} to write to.
     */
    public static void write(InputStream from, OutputStream to) {
        try {
            byte[] bytes = new byte[1024];
            int nRead;
            while ((nRead = from.read(bytes, 0, bytes.length)) != -1) {
                to.write(bytes, 0, nRead);
            }
        } catch (IOException ioe) {
            throw new RuntimeIOException(ioe);
        }
    }

    public static void write(Reader from, Writer to) {
        try {
            char[] chars = new char[1024];
            int nRead;
            while ((nRead = from.read(chars, 0, chars.length)) != -1) {
                to.write(chars, 0, nRead);
            }
        } catch (IOException ioe) {
            throw new RuntimeIOException(ioe);
        }
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

    public static void closeQuietly(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException e) {
            logger.warn("IOException while closing closeable", e);
            // Will not happen because of true...
        }
    }

    public static void checkArgument(boolean expression, String errorMessage, Object... messageParams) {
        if (!expression) {
            throw new IllegalArgumentException(format(errorMessage, messageParams));
        }
    }

    public static void checkState(boolean expression, String errorMessage, Object... messageParams) {
        if (!expression) {
            throw new IllegalStateException(format(errorMessage, messageParams));
        }
    }

    public static <T> T checkNotNull(T t, String errorMessage, Object... messageParams) {
        if (t == null) {
            throw new NullPointerException(format(errorMessage, messageParams));
        }
        return t;
    }

    public static String mkString(List<String> strings, char sep) {
        return mkString(strings, String.valueOf(sep));
    }

    public static String mkString(List<String> strings, String sep) {
        if (strings.isEmpty()) return "";

        StringBuilder b = new StringBuilder(strings.get(0));
        for (int i = 1; i < strings.size(); i++) {
             b.append(sep).append(strings.get(i));
        }
        return b.toString();
    }

    /**
     * Generates unique directory inside the specified base directory.
     * @param baseDir directory where unique directory will be situated
     * @return generated unique directory in the base directory
     */
    public static OverthereFile getUniqueFolder(OverthereFile baseDir) {
        String temporaryFileHolderDirectoryNamePrefix = "ot-" + (new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS")).format(new Date());
        return getUniqueFolder(baseDir, temporaryFileHolderDirectoryNamePrefix);
    }

    /**
     * Generates unique directory with the directoryNameBase as prefix inside of specified base directory.
     * @param baseDir directory where unique directory will be situated
     * @param directoryNameBase The basename the directory.
     * @return generated unique directory in the base directory
     */
    public static OverthereFile getUniqueFolder(OverthereFile baseDir, String directoryNameBase) {
        ConnectionOptions options = baseDir.getConnection().getOptions();
        int temporaryFileCreationRetries = options.getInteger(TEMPORARY_FILE_CREATION_RETRIES, TEMPORARY_FILE_CREATION_RETRIES_DEFAULT);

        if (!baseDir.exists()) {
          throw new RuntimeIOException("Cannot create unique directory in non existing basedir " + baseDir);
        }

        RuntimeException originalExc = null;
        int salt = new Random().nextInt(10000);
        for (int i = 0; i <= temporaryFileCreationRetries; i++) {
            salt += 1;
            OverthereFile holder = baseDir.getFile(directoryNameBase + "." + salt);
            if (!holder.exists()) {
                logger.trace("Creating unique directory {}", holder);
                try {
                    holder.mkdir();
                    return holder;
                } catch(RuntimeException exc) {
                    originalExc = exc;
                    logger.debug(format("Failed to create holder directory %s - Trying with the next suffix", holder), exc);
                }
            } else {
              logger.trace("Could not create new unique directory '{}' as it exists already.'", holder);
            }
        }

        String errorText = "Cannot generate a unique directory on " + baseDir;
        if(originalExc != null) {
            throw new RuntimeIOException(errorText, originalExc);
        } else {
            throw new RuntimeIOException(errorText);
        }
    }
}
