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
package com.xebialabs.overthere.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.OverthereFileCopier;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A file system object (file, directory, etc.) on a remote system that is accessible through an
 * {@link com.xebialabs.overthere.OverthereConnection}.
 */
public abstract class BaseOverthereFile<C extends BaseOverthereConnection> implements OverthereFile {

    protected C connection;

    protected BaseOverthereFile() {
        this.connection = null;
    }

    protected BaseOverthereFile(C connection) {
        this.connection = connection;
    }

    @Override
    public C getConnection() {
        return connection;
    }

    @Override
    public OverthereFile getFile(String child) {
        return getConnection().getFile(this, child);
    }

    @Override
    public void deleteRecursively() throws RuntimeIOException {
        if (isDirectory()) {
            for (OverthereFile each : listFiles()) {
                each.deleteRecursively();
            }
        }

        delete();
    }

    @Override
    public final void copyTo(final OverthereFile dest) {
        checkArgument(dest instanceof BaseOverthereFile<?>, "dest is not a subclass of BaseOverthereFile");

        if (getConnection().equals(dest.getConnection())) {
            ((BaseOverthereFile<?>) dest).localCopyFrom(this);
        } else {
            ((BaseOverthereFile<?>) dest).copyFrom(this);
        }
    }

    protected void copyFrom(OverthereFile source) {
        OverthereFileCopier.copy(source, this);
    }

    /**
     * Copies this file or directory (recursively) to a (new) destination in the same connection.
     *
     * @param source The source file or directory
     */
    protected void localCopyFrom(OverthereFile source) {
        OverthereFile dest = this;
        if (isDirectory() && source.isDirectory() && getName().equals(source.getName())) {
            dest = getParentFile();
        }

        checkArgument(!exists() || (source.isDirectory() == dest.isDirectory()), "Cannot local copy files into directories or vice-versa for [source %s %s to destination %s %s]", typeOf(source), source.getPath(), typeOf(this), getPath());
        OperatingSystemFamily hostOperatingSystem = source.getConnection().getHostOperatingSystem();
        CmdLine cmdLine = new CmdLine();
        String defaultValue = null;
        switch (hostOperatingSystem) {
            case WINDOWS:
                defaultValue = ConnectionOptions.LOCAL_COPY_COMMAND_WINDOWS_DEFAULT_VALUE;
                break;
            case UNIX:
                defaultValue = ConnectionOptions.LOCAL_COPY_COMMAND_UNIX_DEFAULT_VALUE;
                break;
            case ZOS:
                defaultValue = ConnectionOptions.LOCAL_COPY_COMMAND_ZOS_DEFAULT_VALUE;
                break;
        }

        String commandTemplate = getConnection().getOptions().get(ConnectionOptions.LOCAL_COPY_COMMAND, defaultValue);
        cmdLine.addTemplatedFragment(commandTemplate, source.getPath(), dest.getPath());

        logger.debug("Going to execute command [{}] on [{}]", cmdLine, source.getConnection());
        source.getConnection().execute(cmdLine);
    }

    private String typeOf(OverthereFile overthereFile) {
        return overthereFile.isDirectory() ? "directory" : "file";
    }


    /**
     * Subclasses MUST implement toString properly.
     */
    @Override
    public abstract String toString();

    private static final Logger logger = LoggerFactory.getLogger(BaseOverthereFile.class);

}
