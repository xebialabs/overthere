/*
 * Copyright (c) 2008-2013, XebiaLabs B.V., All rights reserved.
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

        ((BaseOverthereFile<?>) dest).copyFrom(this);
    }

    protected void copyFrom(OverthereFile source) {
        OverthereFileCopier.copy(source, this);
    }

    /**
     * Subclasses MUST implement toString properly.
     */
    @Override
    public abstract String toString();

}
