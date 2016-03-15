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
package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.OverthereFile;
import net.schmizz.sshj.xfer.LocalFileFilter;
import net.schmizz.sshj.xfer.LocalSourceFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for the LocalSourceFile supplied by SSHJ.
 */
class OverthereFileLocalSourceFile implements LocalSourceFile {

    private OverthereFile f;

    public OverthereFileLocalSourceFile(OverthereFile f) {
        this.f = f;
    }

    @Override
    public String getName() {
        return f.getName();
    }

    @Override
    public long getLength() {
        return f.length();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return f.getInputStream();
    }

    @Override
    public int getPermissions() throws IOException {
        return f.isDirectory() ? 0755 : 0644;
    }

    @Override
    public boolean isFile() {
        return f.isFile();
    }

    @Override
    public boolean isDirectory() {
        return f.isDirectory();
    }

    @Override
    public Iterable<? extends LocalSourceFile> getChildren(LocalFileFilter filter) throws IOException {
        List<LocalSourceFile> files = new ArrayList<LocalSourceFile>();
        for (OverthereFile each : f.listFiles()) {
            files.add(new OverthereFileLocalSourceFile(each));
        }
        return files;
    }

    @Override
    public boolean providesAtimeMtime() {
        return false;
    }

    @Override
    public long getLastAccessTime() throws IOException {
        return 0;
    }

    @Override
    public long getLastModifiedTime() throws IOException {
        return 0;
    }

}
