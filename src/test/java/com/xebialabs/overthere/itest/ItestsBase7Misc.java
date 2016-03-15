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

import java.io.File;
import java.io.IOException;
import org.testng.annotations.Test;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.local.LocalFile;

import nl.javadude.assumeng.Assumption;

import static com.xebialabs.overthere.local.LocalConnection.getLocalConnection;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.fail;

public abstract class ItestsBase7Misc extends ItestsBase6Windows {

    @Test(enabled = false, description = "Test fails for WinRM because startProcess does not throw an UnsupportedOperationException even though canStartProcess returns false. Enable test when WinRM streaming is properly implemented.")
    @Assumption(methods = {"notSupportsProcess"})
    public void shouldStartProcessShouldThrowExceptionWhenNotSupported() {
        try {
            connection.startProcess(CmdLine.build("echo"));
            fail("Expected UnsupportedOperationException to be thrown");
        } catch (UnsupportedOperationException expected) {
        }
    }

    @Test
    public void shouldCopyFileWithSpaceInNameToNonTempLocation() throws IOException {
        File fileWithSpaces = temp.newFile("I have spaces.txt");
        writeRandomBytes(fileWithSpaces, 100);

        OverthereFile dir = connection.getTempFile("dir");
        OverthereFile targetDir = connection.getFile(dir.getPath() + "/newDir");
        targetDir.mkdirs();

        OverthereFile targetFile = connection.getFile(targetDir.getPath() + "/" + fileWithSpaces.getName());

        LocalFile.valueOf(fileWithSpaces).copyTo(targetFile);
        try {
            assertThat(targetFile.exists(), is(true));
        } finally {
            // When using a sudo connection, the target folder has different rights to the temp folder it was created in.
            targetDir.deleteRecursively();
        }
    }

    @Test
    public void shouldTruncateExistingTargetFileOnCopyFromLocal() throws Exception {
        final OverthereFile existingDestination = connection.getFile(connection.getTempFile("existing").getPath());
        writeData(existingDestination, "**********\n**********\n**********\n**********\n**********\n".getBytes());

        final OverthereFile newSource = getLocalConnection().getTempFile("newContents");
        writeData(newSource, "++++++++++".getBytes());

        newSource.copyTo(existingDestination);

        byte[] bytes = readFile(existingDestination);
        assertThat(bytes.length, equalTo(10));
        assertThat(bytes, equalTo("++++++++++".getBytes()));
    }

}
