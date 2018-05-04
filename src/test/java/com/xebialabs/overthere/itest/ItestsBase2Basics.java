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
package com.xebialabs.overthere.itest;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.testng.annotations.Test;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.OverthereUtils;

import nl.javadude.assumeng.Assumption;

import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.fail;

public abstract class ItestsBase2Basics extends ItestsBase1Utils {

    @Test
    public void connectionObjectShouldBeInstanceOfExpectedClass() {
        assertThat(connection.getClass().getName(), equalTo(expectedConnectionClassName));
    }

    @Test
    @Assumption(methods = {"notLocal", "notCifs"})
    public void shouldNotConnectWithIncorrectUsername() {
        ConnectionOptions incorrectUserNameOptions = new ConnectionOptions(options);
        incorrectUserNameOptions.set(USERNAME, "an-incorrect-username");
        try {
            Overthere.getConnection(protocol, incorrectUserNameOptions);
            fail("Expected not to be able to connect with an incorrect username");
        } catch (RuntimeIOException expected) {
        }
    }

    @Test
    @Assumption(methods = {"notLocal", "notCifs", "withPassword"})
    public void shouldNotConnectWithIncorrectPassword() {
        ConnectionOptions incorrectPasswordOptions = new ConnectionOptions(options);
        incorrectPasswordOptions.set(PASSWORD, "an-incorrect-password");
        try {
            Overthere.getConnection(protocol, incorrectPasswordOptions);
            fail("Expected not to be able to connect with an incorrect password");
        } catch (RuntimeIOException expected) {
        }
    }

    @Test
    public void shouldCreateWriteReadAndRemoveTemporaryFile() throws IOException {
        final String prefix = "prefix";
        final String suffix = "suffix";
        final byte[] contents = ("Contents of the temporary file created at " + System.currentTimeMillis() + "ms since the epoch").getBytes();

        OverthereFile tempFile = connection.getTempFile(prefix, suffix);
        assertThat("Expected a non-null return value from HostConnection.getTempFile()", tempFile, notNullValue());
        assertThat("Expected name of temporary file to start with the prefix", tempFile.getName(), startsWith(prefix));
        assertThat("Expected name of temporary file to end with the suffix", tempFile.getName(), endsWith(suffix));
        assertThat("Expected temporary file to not exist yet", tempFile.exists(), equalTo(false));

        OutputStream out = tempFile.getOutputStream();
        try {
            out.write(contents);
        } finally {
            closeQuietly(out);
        }

        assertThat("Expected temporary file to exist after writing to it", tempFile.exists(), equalTo(true));
        assertThat("Expected temporary file to not be a directory", tempFile.isDirectory(), equalTo(false));
        assertThat("Expected temporary file to have the size of the contents written to it", tempFile.length(), equalTo((long) contents.length));
        assertThat("Expected temporary file to be readable", tempFile.canRead(), equalTo(true));
        assertThat("Expected temporary file to be writeable", tempFile.canWrite(), equalTo(true));

        // Windows systems don't support the concept of checking for executability
        if (connection.getHostOperatingSystem() == OperatingSystemFamily.UNIX) {
            assertThat("Expected temporary file to not be executable", !tempFile.canExecute());
        }

        DataInputStream in = new DataInputStream(tempFile.getInputStream());
        try {
            final byte[] contentsRead = new byte[contents.length];
            in.readFully(contentsRead);
            assertThat("Expected input stream to be exhausted after reading the full contents", in.available(), equalTo(0));
            assertThat("Expected contents in temporary file to be identical to data written into it", contentsRead, equalTo(contents));
        } finally {
            closeQuietly(in);
        }

        tempFile.delete();
        assertThat("Expected temporary file to no longer exist", tempFile.exists(), equalTo(false));
    }

    @Test
    public void shouldCreatePopulateListAndRemoveTemporaryDirectory() {
        final String prefix = "prefix";
        final String suffix = "suffix";

        OverthereFile tempDir = connection.getTempFile(prefix, suffix);
        assertThat("Expected a non-null return value from HostConnection.getTempFile()", tempDir, notNullValue());
        assertThat("Expected name of temporary file to start with the prefix", tempDir.getName(), startsWith(prefix));
        assertThat("Expected name of temporary file to end with the suffix", tempDir.getName(), endsWith(suffix));
        assertThat("Expected temporary file to not exist yet", tempDir.exists(), equalTo(false));

        tempDir.mkdir();
        assertThat("Expected temporary directory to exist after creating it", tempDir.exists(), equalTo(true));
        assertThat("Expected temporary directory to be a directory", tempDir.isDirectory(), equalTo(true));

        OverthereFile anotherTempDir = connection.getTempFile(prefix, suffix);
        assertThat("Expected temporary directories created with identical prefix and suffix to still be different", tempDir.getPath(),
                not(equalTo(anotherTempDir.getPath())));

        OverthereFile nested1 = tempDir.getFile("nested1");
        OverthereFile nested2 = nested1.getFile("nested2");
        OverthereFile nested3 = nested2.getFile("nested3");
        assertThat("Expected deeply nested directory to not exist", nested3.exists(), equalTo(false));

        nested3.mkdirs();
        assertThat("Expected deeply nested directory to exist after invoking mkdirs on it", nested3.exists(), equalTo(true));

        final byte[] contents = ("Contents of the temporary file created at " + System.currentTimeMillis() + "ms since the epoch").getBytes();
        OverthereFile regularFile = tempDir.getFile("somefile.txt");
        OverthereUtils.write(contents, regularFile);

        List<OverthereFile> dirContents = tempDir.listFiles();
        assertThat(dirContents, containsInAnyOrder(nested1, regularFile));
        assertThat(dirContents, hasSize(2));

        try {
            nested1.delete();
        } catch (RuntimeIOException expected2) {
        }
        nested1.deleteRecursively();
        assertThat("Expected parent of deeply nested directory to have been removed recursively", nested1.exists(), equalTo(false));

        regularFile.delete();
        tempDir.delete();
        assertThat("Expected temporary directory to not exist after removing it when it was empty", tempDir.exists(), equalTo(false));
    }

    /*
    * This test is ignored on WinSSHD because in new and supported versions of WinSSHD it is possible to create
    * nested folders in one go (mkdir nest1/nest2/nest3 is possible). In all other implementations of sftp creating
    * nested folders in one go is not possible.
    */

    @Test
    @Assumption(methods = {"notSftpWinsshd"})
    public void shouldNotCreateTemporaryDirectoriesRecursively() {
        final String prefix = "prefix";
        final String suffix = "suffix";

        OverthereFile tempDir = connection.getTempFile(prefix, suffix);
        OverthereFile nested1 = tempDir.getFile("nested1");
        OverthereFile nested2 = nested1.getFile("nested2");
        OverthereFile nested3 = nested2.getFile("nested3");
        assertThat("Expected deeply nested directory to not exist", nested3.exists(), equalTo(false));
        try {
            nested3.mkdir();
            fail("Expected not to be able to create a deeply nested directory in one go");
        } catch (RuntimeIOException expected1) {
        }
        assertThat("Expected deeply nested directory to still not exist", nested3.exists(), equalTo(false));
        assertThat("Expected temporary directory to not exist after removing it when it was empty", tempDir.exists(), equalTo(false));
    }

}