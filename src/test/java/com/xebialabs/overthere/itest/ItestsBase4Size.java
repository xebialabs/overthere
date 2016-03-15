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
import java.util.Arrays;
import org.testng.annotations.Test;

import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.local.LocalFile;
import com.xebialabs.overthere.util.OverthereUtils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class ItestsBase4Size extends ItestsBase3Copy {

    private static final int LARGE_FILE_SIZE = 1 * 1024 * 1024;
    /* FIXME: Set LARGE_FILE_SIZE back to 100MB when running the itest on a local KVM host. */

    private static final int NR_OF_SMALL_FILES = 256;
    private static final int SMALL_FILE_SIZE = 10 * 1024;

    @Test
    public void shouldWriteLargeFile() throws IOException {
        byte[] expected = generateRandomBytes(LARGE_FILE_SIZE);

        OverthereFile remoteLargeFile = connection.getTempFile("large.dat");
        OverthereUtils.write(expected, remoteLargeFile);

        byte[] actual = readFile(remoteLargeFile);
        assertThat("Data read is not identical to data written", Arrays.equals(actual, expected), equalTo(true));
    }

    @Test
    public void shouldCopyLargeFile() throws IOException {
        File largeFile = temp.newFile("large.dat");
        byte[] expected = writeRandomBytes(largeFile, LARGE_FILE_SIZE);

        OverthereFile remoteLargeFile = connection.getTempFile("large.dat");
        LocalFile.valueOf(largeFile).copyTo(remoteLargeFile);

        byte[] actual = readFile(remoteLargeFile);
        assertThat("Data read is not identical to data written", Arrays.equals(actual, expected), equalTo(true));
    }


    @Test
    public void shouldCopyDirectoryWithManyFiles() throws IOException {
        File largeFolder = temp.newFolder("small.folder");
        for (int i = 0; i < NR_OF_SMALL_FILES; i++) {
            writeRandomBytes(new File(largeFolder, "small" + i + ".dat"), SMALL_FILE_SIZE);
        }

        OverthereFile remoteLargeFolder = connection.getTempFile("small.folder");
        LocalFile.valueOf(largeFolder).copyTo(remoteLargeFolder);

        for (int i = 0; i < NR_OF_SMALL_FILES; i++) {
            OverthereFile remoteFile = remoteLargeFolder.getFile("small" + i + ".dat");
            byte[] remoteBytes = OverthereUtils.read(remoteFile);
            assertThat(remoteBytes.length, equalTo(SMALL_FILE_SIZE));
        }
    }

}
