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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class SshScpFileTest {

    private SshScpConnection connection;
    private SshScpFile sshScpFile;

    @BeforeClass
    public void setup() {
        connection = mock(SshScpConnection.class);
        sshScpFile = new SshScpFile(connection, "/foo/bar");
    }

    @Test
    public void shouldParseDirectoryWithAclOnLs() {
        SshScpFile.LsResults results = new SshScpFile.LsResults();
        sshScpFile.parseLsOutputLine(results, "drwxr-xr-x+ 10 ajvanerp  staff    340 Dec 17 15:28 build");
        assertThat("Should be a directory", results.isDirectory);
        assertThat("Should be executable", results.canExecute);
    }

    @Test
    public void shouldParseDirectoryWithMacOSExtendedAttrsOnLs() {
        SshScpFile.LsResults results = new SshScpFile.LsResults();
        sshScpFile.parseLsOutputLine(results, "drwxr-xr-x@ 10 ajvanerp  staff    340 Dec 17 15:28 build");
        assertThat("Should be a directory", results.isDirectory);
        assertThat("Should be executable", results.canExecute);
    }

    @Test
    public void shouldParseDirectoryWithEndingDotOnLs() {
        SshScpFile.LsResults results = new SshScpFile.LsResults();
        sshScpFile.parseLsOutputLine(results, "drwxr-xr-x. 10 ajvanerp  staff    340 Dec 17 15:28 build");
        assertThat("Should be a directory", results.isDirectory);
        assertThat("Should be executable", results.canExecute);
    }

    @Test
    public void shouldParseSymLinkOnLs() {
        SshScpFile.LsResults results = new SshScpFile.LsResults();
        assertThat("Should parse", sshScpFile.parseLsOutputLine(results, "lrwxr-xr-x 10 ajvanerp  staff    340 Dec 17 15:28 /var/build -> build"));
        assertThat("Should be a directory", !results.isDirectory);
        assertThat("Should be executable", results.canExecute);
    }

    @Test
    public void shouldParseLsResultsPrefixedWithEscapeCodes() {
        SshScpFile.LsResults results = new SshScpFile.LsResults();
        assertThat("Should parse", sshScpFile.parseLsOutputLine(results, "\033H\0332Jdrwxr-xr-x 10 ajvanerp  staff    340 Dec 17 15:28 build"));
        assertThat("Should be a directory", results.isDirectory);
        assertThat("Should be executable", results.canExecute);
    }
}
