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
package com.xebialabs.overthere.ssh;

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.xebialabs.overthere.*;
import com.xebialabs.overthere.spi.BaseOverthereFile;

import java.util.List;

/**
 * A file on a host connected through SSH.
 */
abstract class SshFile<C extends SshConnection> extends BaseOverthereFile<C> {

    protected List<String> pathComponents;

    /**
     * Constructs an SshOverthereFile
     * 
     * @param connection
     *            the connection to the host
     * @param path
     *            the path of the file on the host
     */
    SshFile(C connection, String path) {
        this(connection, splitPath(path, connection.getHostOperatingSystem()));
    }

    SshFile(C connection, List<String> pathComponents) {
        super(connection);
        checkWindowsPath(pathComponents, connection.getHostOperatingSystem());
        this.pathComponents = pathComponents;
    }

    @Override
    public String getPath() {
        return joinPath(pathComponents, connection.getHostOperatingSystem());
    }

    @Override
    public boolean isHidden() {
        return getName().startsWith(".");
    }

    @Override
    public String getName() {
        if(pathComponents.isEmpty()) {
            return connection.getHostOperatingSystem().getFileSeparator();
        } else {
            return pathComponents.get(pathComponents.size() - 1);
        }
    }

    @Override
    public OverthereFile getParentFile() {
        if(pathComponents.isEmpty()) {
            // The root path is its own parent.
            return this;
        }

        if(connection.getHostOperatingSystem() == WINDOWS && pathComponents.size() == 1) {
            // On Windows, the drive path is its own parent
            return this;
        }

        return connection.getFile(joinPath(pathComponents.subList(0, pathComponents.size() - 1), connection.getHostOperatingSystem()));
    }

    @Override
    public void delete() throws RuntimeIOException {
        if (exists()) {
            if (isDirectory()) {
                deleteDirectory();
            } else {
                deleteFile();
            }
        }
    }

    protected abstract void deleteFile();

    protected abstract void deleteDirectory();

    protected int executeCommand(OverthereExecutionOutputHandler outHandler, OverthereExecutionOutputHandler errHandler, CmdLine commandLine) {
        return connection.execute(outHandler, errHandler, commandLine);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SshFile)) {
            return false;
        }

        return getPath().equals(((SshFile<?>) obj).getPath());
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    @Override
    public String toString() {
        String p = getPath();
        if (p.length() >= 1 && p.charAt(0) == '/') {
            return getConnection() + p;
        } else {
            return getConnection() + "/" + p;
        }
    }

    static List<String> splitPath(String path, OperatingSystemFamily os) {
        Splitter s = os == WINDOWS ? WINDOWS_PATH_SPLITTER : UNIX_PATH_SPLITTER;
        return newArrayList(s.split(path));
    }

    static String joinPath(List<String> pathComponents, OperatingSystemFamily os) {
        String fileSep = os.getFileSeparator();

        if(pathComponents.isEmpty()) {
            return fileSep;
        }

        if(os == WINDOWS) {
            String path = Joiner.on(fileSep).join(pathComponents);
            if(pathComponents.size() == 1) {
                path += fileSep;
            }
            return path;
        } else {
            return fileSep + Joiner.on(fileSep).join(pathComponents);
        }
    }

    static void checkWindowsPath(List<String> pathComponents, OperatingSystemFamily os) {
        if(os == WINDOWS && pathComponents.isEmpty()) {
            throw new IllegalArgumentException("Empty path is not allowed on Windows");
        }
    }

    private static final Splitter UNIX_PATH_SPLITTER = Splitter.on('/').omitEmptyStrings();

    private static final Splitter WINDOWS_PATH_SPLITTER = Splitter.on(CharMatcher.anyOf("/\\")).omitEmptyStrings();

}
