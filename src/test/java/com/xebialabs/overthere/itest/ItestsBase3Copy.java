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
package com.xebialabs.overthere.itest;

import java.util.Arrays;
import org.testng.annotations.Test;

import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.ssh.SshConnectionBuilder;
import com.xebialabs.overthere.ssh.SshConnectionType;

import static com.xebialabs.overthere.local.LocalConnection.getLocalConnection;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionType.INTERACTIVE_SUDO;
import static com.xebialabs.overthere.ssh.SshConnectionType.SUDO;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class ItestsBase3Copy extends ItestsBase2Basics {

    private static final String SOURCE_DIR_NAME = "dir-to-copy";

    private static final String DESTINATION_DIR_ALTERNATIVE_NAME = "dir-to-copy-with-different-name";

    private static byte[] SOURCE_FILE_CONTENTS = "This file should be copied".getBytes();

    private static final String SOURCE_FILE_NAME = "file-to-copy.txt";

    private static final String DESTINATION_FILE_ALTERNATIVE_NAME = "file-to-copy-with-different-name.txt";

    private static byte[] EXISTENT_DEST_FILE_CONTENT = "This file should be overwritten".getBytes();

    private static final String OTHER_DEST_FILE_NAME = "file-to-be-left-as-is.txt";

    private static byte[] OTHER_DEST_FILE_CONTENT = "This should be left as-is".getBytes();

    @Test
    public void shouldCopyLocalFileToNonExistentRemoteFile() {
        OverthereFile srcFile = getLocalSourceFile();
        OverthereFile dstFile = getRemoteDestinationFile();

        populateSourceFile(srcFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyLocalFileToExistentRemoteFile() {
        OverthereFile srcFile = getLocalSourceFile();
        OverthereFile dstFile = getRemoteDestinationFile();

        populateSourceFile(srcFile);
        populateExistentDestinationFile(dstFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyLocalFileToNonExistentRemoteFileWithDifferentName() {
        OverthereFile srcFile = getLocalSourceFile();
        OverthereFile dstFile = getRemoteDestinationFileWithDifferentName();

        populateSourceFile(srcFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyLocalFileToExistentRemoteFileWithDifferentName() {
        OverthereFile srcFile = getLocalSourceFile();
        OverthereFile dstFile = getRemoteDestinationFileWithDifferentName();

        populateSourceFile(srcFile);
        populateExistentDestinationFile(dstFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyRemoteFileToNonExistentRemoteFile() {
        OverthereFile srcFile = getRemoteSourceFile();
        OverthereFile dstFile = getRemoteDestinationFile();

        populateSourceFile(srcFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyRemoteFileToExistentRemoteFile() {
        OverthereFile srcFile = getRemoteSourceFile();
        OverthereFile dstFile = getRemoteDestinationFile();

        populateSourceFile(srcFile);
        populateExistentDestinationFile(dstFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyRemoteFileToNonExistentRemoteFileWithDifferentName() {
        OverthereFile srcFile = getRemoteSourceFile();
        OverthereFile dstFile = getRemoteDestinationFileWithDifferentName();

        populateSourceFile(srcFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyRemoteFileToExistentRemoteFileWithDifferentName() {
        OverthereFile srcFile = getRemoteSourceFile();
        OverthereFile dstFile = getRemoteDestinationFileWithDifferentName();

        populateSourceFile(srcFile);
        populateExistentDestinationFile(dstFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyLocalDirectoryToNonExistentRemoteDirectory() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectory();

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyLocalDirectoryToExistentRemoteDirectory() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectory();

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyLocalDirectoryToNonExistentRemoteDirectoryWithDifferentName() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectoryWithDifferentName();

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyLocalDirectoryToExistentRemoteDirectoryWithDifferentName() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectoryWithDifferentName();

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToNonExistentRemoteDirectory() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectory();

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToExistentRemoteDirectory() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectory();

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToNonExistentRemoteDirectoryWithDifferentName() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectoryWithDifferentName();

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToExistentRemoteDirectoryWithDifferentName() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectoryWithDifferentName();

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyLocalDirectoryToNonExistentRemoteDirectoryWithTrailingFileSeparator() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = appendFileSeparator(getRemoteDestinationDirectory());

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyLocalDirectoryToExistentRemoteDirectoryWithTrailingFileSeparator() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = appendFileSeparator(getRemoteDestinationDirectory());

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyLocalDirectoryToNonExistentRemoteDirectoryWithDifferentNameAndTrailingFileSeparator() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = appendFileSeparator(getRemoteDestinationDirectoryWithDifferentName());

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyLocalDirectoryToExistentRemoteDirectoryWithDifferentNameAndTrailingFileSeparator() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = appendFileSeparator(getRemoteDestinationDirectoryWithDifferentName());

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToNonExistentRemoteDirectoryWithTrailingFileSeparator() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = appendFileSeparator(getRemoteDestinationDirectory());

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToExistentRemoteDirectoryWithTrailingFileSeparator() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = appendFileSeparator(getRemoteDestinationDirectory());

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToNonExistentRemoteDirectoryWithDifferentNameAndTrailingFileSeparator() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = appendFileSeparator(getRemoteDestinationDirectoryWithDifferentName());

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToExistentRemoteDirectoryWithDifferentNameAndTrailingFileSeparator() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = appendFileSeparator(getRemoteDestinationDirectoryWithDifferentName());

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    private OverthereFile getLocalSourceFile() {
        return getLocalConnection().getTempFile(SOURCE_FILE_NAME);
    }

    private OverthereFile getRemoteSourceFile() {
        return connection.getTempFile(SOURCE_FILE_NAME);
    }

    private OverthereFile getRemoteDestinationFile() {
        return connection.getFile(connection.getTempFile(SOURCE_FILE_NAME).getPath());
    }

    private OverthereFile getRemoteDestinationFileWithDifferentName() {
        return connection.getFile(connection.getTempFile(DESTINATION_FILE_ALTERNATIVE_NAME).getPath());
    }

    private void populateSourceFile(OverthereFile srcFile) {
        writeData(srcFile, SOURCE_FILE_CONTENTS);
    }

    private void populateExistentDestinationFile(OverthereFile dstFile) {
        writeData(dstFile, EXISTENT_DEST_FILE_CONTENT);
    }

    private void assertSourceFileWasCopiedToDestinationFile(OverthereFile dstFile) {
        dstFile = connection.getFile(dstFile.getPath());

        assertFile(dstFile, SOURCE_FILE_CONTENTS);
    }

    private OverthereFile getLocalSourceDirectory() {
        return getLocalConnection().getTempFile(SOURCE_DIR_NAME);
    }

    private OverthereFile getRemoteSourceDirectory() {
        return connection.getTempFile(SOURCE_DIR_NAME);
    }

    private OverthereFile getRemoteDestinationDirectory() {
        return connection.getFile(connection.getTempFile(SOURCE_DIR_NAME).getPath());
    }

    private OverthereFile getRemoteDestinationDirectoryWithDifferentName() {
        return connection.getFile(connection.getTempFile(DESTINATION_DIR_ALTERNATIVE_NAME).getPath());
    }

    private OverthereFile appendFileSeparator(OverthereFile dir) {
        return connection.getFile(dir.getPath() + connection.getHostOperatingSystem().getFileSeparator());
    }

    private void populateSourceDirectory(OverthereFile srcDir) {
        srcDir.mkdir();
        OverthereFile fileInSrcDir = srcDir.getFile(SOURCE_FILE_NAME);
        writeData(fileInSrcDir, SOURCE_FILE_CONTENTS);
    }

    private void populateExistentDestinationDirectory(OverthereFile dstDir) {
        dstDir.mkdir();
        OverthereFile otherFileInDestDir = dstDir.getFile(OTHER_DEST_FILE_NAME);
        writeData(otherFileInDestDir, OTHER_DEST_FILE_CONTENT);
    }

    private void assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(OverthereFile dstDir) {
        dstDir = connection.getFile(dstDir.getPath());

        assertDir(dstDir);
        assertFile(dstDir.getFile(SOURCE_FILE_NAME), SOURCE_FILE_CONTENTS);
        assertThat(dstDir.getFile(OTHER_DEST_FILE_NAME).exists(), is(false));
    }

    private void assertSourceDirectoryWasCopiedToExistentDestinationDirectory(OverthereFile dstDir) {
        dstDir = connection.getFile(dstDir.getPath());

        assertDir(dstDir);
        assertFile(dstDir.getFile(SOURCE_FILE_NAME), SOURCE_FILE_CONTENTS);
        assertFile(dstDir.getFile(OTHER_DEST_FILE_NAME), OTHER_DEST_FILE_CONTENT);
    }

    private void assertDir(final OverthereFile dir) {
        assertThat(format("Directory [%s] does not exist", dir), dir.exists(), is(true));
        assertThat(format("Directory [%s] is not a directory", dir), dir.isDirectory(), is(true));
    }

    private void assertFile(final OverthereFile file, final byte[] expectedContents) {
        assertThat(format("File [%s] does not exist", file), file.exists(), is(true));
        assertThat(format("File [%s] is not a regular file", file), file.isFile(), is(true));
        byte[] actualContents = readFile(file);
        assertThat(format("File [%s] does not have the expected contents", file), Arrays.equals(actualContents, expectedContents), is(true));
    }

}
