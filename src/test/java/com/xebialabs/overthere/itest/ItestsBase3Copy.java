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

import java.util.Arrays;
import org.testng.annotations.Test;

import com.xebialabs.overthere.OverthereFile;
import static com.xebialabs.overthere.local.LocalConnection.getLocalConnection;
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

    /**
     * Test copies from a local file to a remote file.
     */

    @Test
    public void shouldCopyLocalFileToNonExistentRemoteFile() {
        shouldCopyToNonExistentFile(getLocalSourceFile(), getRemoteDestinationFile());
    }

    @Test
    public void shouldCopyLocalFileToExistentRemoteFile() {
        shouldCopyToExistentFile(getLocalSourceFile(), getRemoteDestinationFile());
    }

    @Test
    public void shouldCopyLocalFileToNonExistentRemoteFileWithDifferentName() {
        shouldCopyToNonExistentFile(getLocalSourceFile(), getRemoteDestinationFileWithDifferentName());
    }

    @Test
    public void shouldCopyLocalFileToExistentRemoteFileWithDifferentName() {
        shouldCopyToExistentFile(getLocalSourceFile(), getRemoteDestinationFileWithDifferentName());
    }


    /**
     * Test copies from a remote file to a remote file.
     */

    @Test
    public void shouldCopyRemoteFileToNonExistentRemoteFile() {
        shouldCopyToNonExistentFile(getRemoteSourceFile(), getRemoteDestinationFile());
    }

    @Test
    public void shouldCopyRemoteFileToExistentRemoteFile() {
        shouldCopyToExistentFile(getRemoteSourceFile(), getRemoteDestinationFile());
    }

    @Test
    public void shouldCopyRemoteFileToNonExistentRemoteFileWithDifferentName() {
        shouldCopyToNonExistentFile(getRemoteSourceFile(), getRemoteDestinationFileWithDifferentName());
    }

    @Test
    public void shouldCopyRemoteFileToExistentRemoteFileWithDifferentName() {
        shouldCopyToExistentFile(getRemoteSourceFile(), getRemoteDestinationFileWithDifferentName());
    }

    /**
     * Test copies from a remote (temporary) file to a remote (temporary) file.
     */

    @Test
    public void shouldCopyRemoteFileToNonExistentRemoteTempFile() {
        shouldCopyToNonExistentFile(getRemoteSourceFile(), getRemoteTempDestinationFile());
    }

    @Test
    public void shouldCopyRemoteFileToExistentRemoteTempFile() {
        shouldCopyToExistentFile(getRemoteSourceFile(), getRemoteTempDestinationFile());
    }

    @Test
    public void shouldCopyRemoteTempFileToNonExistentRemoteFile() {
        shouldCopyToNonExistentFile(getRemoteTempSourceFile(), getRemoteDestinationFile());
    }

    @Test
    public void shouldCopyRemoteTempFileToExistentRemoteFile() {
        shouldCopyToExistentFile(getRemoteTempSourceFile(), getRemoteDestinationFile());
    }

    @Test
    public void shouldCopyRemoteTempFileToNonExistentRemoteTempFile() {
        shouldCopyToNonExistentFile(getRemoteTempSourceFile(), getRemoteTempDestinationFile());
    }

    @Test
    public void shouldCopyRemoteTempFileToExistentRemoteTempFile() {
        shouldCopyToExistentFile(getRemoteTempSourceFile(), getRemoteTempDestinationFile());
    }

    /**
     * Test copies from a local directory to a remote directory.
     */

    @Test
    public void shouldCopyLocalDirectoryToNonExistentRemoteDirectory() {
        shouldCopyToNonExistentDirectory(getLocalSourceDirectory(), getRemoteDestinationDirectory());
    }

    @Test
    public void shouldCopyLocalDirectoryToExistentRemoteDirectory() {
        shouldCopyToExistentDirectory(getLocalSourceDirectory(), getRemoteDestinationDirectory());
    }

    @Test
    public void shouldCopyLocalDirectoryToNonExistentRemoteDirectoryWithDifferentName() {
        shouldCopyToNonExistentDirectory(getLocalSourceDirectory(), getRemoteDestinationDirectoryWithDifferentName());
    }

    @Test
    public void shouldCopyLocalDirectoryToExistentRemoteDirectoryWithDifferentName() {
        shouldCopyToExistentDirectory(getLocalSourceDirectory(), getRemoteDestinationDirectoryWithDifferentName());
    }

    /**
     * Test copies from a remote directory to a remote directory.
     */

    @Test
    public void shouldCopyRemoteDirectoryToNonExistentRemoteDirectory() {
        shouldCopyToNonExistentDirectory(getRemoteSourceDirectory(), getRemoteDestinationDirectory());
    }

    @Test
    public void shouldCopyRemoteDirectoryToExistentRemoteDirectory() {
        shouldCopyToExistentDirectory(getRemoteSourceDirectory(), getRemoteDestinationDirectory());
    }

    @Test
    public void shouldCopyRemoteDirectoryToNonExistentRemoteDirectoryWithDifferentName() {
        shouldCopyToNonExistentDirectory(getRemoteSourceDirectory(), getRemoteDestinationDirectoryWithDifferentName());
    }

    @Test
    public void shouldCopyRemoteDirectoryToExistentRemoteDirectoryWithDifferentName() {
        shouldCopyToExistentDirectory(getRemoteSourceDirectory(), getRemoteDestinationDirectoryWithDifferentName());
    }

    /**
     * Test copies from a remote directory to a remote directory with a trailing file separator.
     */

    @Test
    public void shouldCopyLocalDirectoryToNonExistentRemoteDirectoryWithTrailingFileSeparator() {
        shouldCopyToNonExistentDirectory(getLocalSourceDirectory(), appendFileSeparator(getRemoteDestinationDirectory()));
    }

    @Test
    public void shouldCopyLocalDirectoryToExistentRemoteDirectoryWithTrailingFileSeparator() {
        shouldCopyToExistentDirectory(getLocalSourceDirectory(), appendFileSeparator(getRemoteDestinationDirectory()));
    }

    @Test
    public void shouldCopyLocalDirectoryToNonExistentRemoteDirectoryWithDifferentNameAndTrailingFileSeparator() {
        shouldCopyToNonExistentDirectory(getLocalSourceDirectory(), appendFileSeparator(getRemoteDestinationDirectoryWithDifferentName()));
    }

    @Test
    public void shouldCopyLocalDirectoryToExistentRemoteDirectoryWithDifferentNameAndTrailingFileSeparator() {
        shouldCopyToExistentDirectory(getLocalSourceDirectory(), appendFileSeparator(getRemoteDestinationDirectoryWithDifferentName()));
    }

    @Test
    public void shouldCopyRemoteDirectoryToNonExistentRemoteDirectoryWithTrailingFileSeparator() {
        shouldCopyToNonExistentDirectory(getRemoteSourceDirectory(), appendFileSeparator(getRemoteDestinationDirectory()));
    }

    @Test
    public void shouldCopyRemoteDirectoryToExistentRemoteDirectoryWithTrailingFileSeparator() {
        shouldCopyToExistentDirectory(getRemoteSourceDirectory(), appendFileSeparator(getRemoteDestinationDirectory()));
    }

    @Test
    public void shouldCopyRemoteDirectoryToNonExistentRemoteDirectoryWithDifferentNameAndTrailingFileSeparator() {
        shouldCopyToNonExistentDirectory(getRemoteSourceDirectory(), appendFileSeparator(getRemoteDestinationDirectoryWithDifferentName()));
    }

    @Test
    public void shouldCopyRemoteDirectoryToExistentRemoteDirectoryWithDifferentNameAndTrailingFileSeparator() {
        shouldCopyToExistentDirectory(getRemoteSourceDirectory(), appendFileSeparator(getRemoteDestinationDirectoryWithDifferentName()));
    }

    /**
     * Test copies from a remote (temporary) directory to a remote (temporary) directory.
     */

    @Test
    public void shouldCopyRemoteDirectoryToNonExistentRemoteTempDirectory() {
        shouldCopyToNonExistentDirectory(getRemoteSourceDirectory(), getRemoteTempDestinationDirectory());
    }

    @Test
    public void shouldCopyRemoteDirectoryToExistentRemoteTempDirectory() {
        shouldCopyToExistentDirectory(getRemoteSourceDirectory(), getRemoteTempDestinationDirectory());
    }

    @Test
    public void shouldCopyRemoteTempDirectoryToNonExistentRemoteDirectory() {
        shouldCopyToNonExistentDirectory(getRemoteTempSourceDirectory(), getRemoteDestinationDirectory());
    }

    @Test
    public void shouldCopyRemoteTempDirectoryToExistentRemoteDirectory() {
        shouldCopyToExistentDirectory(getRemoteTempSourceDirectory(), getRemoteDestinationDirectory());
    }

    @Test
    public void shouldCopyRemoteTempDirectoryToNonExistentRemoteTempDirectory() {
        shouldCopyToNonExistentDirectory(getRemoteTempSourceDirectory(), getRemoteTempDestinationDirectory());
    }

    @Test
    public void shouldCopyRemoteTempDirectoryToExistentRemoteTempDirectory() {
        shouldCopyToExistentDirectory(getRemoteTempSourceDirectory(), getRemoteTempDestinationDirectory());
    }

    private OverthereFile getLocalSourceFile() {
        return getLocalConnection().getTempFile(SOURCE_FILE_NAME);
    }

    private OverthereFile getRemoteSourceFile() {
        return connection.getFile(connection.getTempFile(SOURCE_FILE_NAME).getPath());
    }

    private OverthereFile getRemoteDestinationFile() {
        return connection.getFile(connection.getTempFile(SOURCE_FILE_NAME).getPath());
    }

    private OverthereFile getRemoteTempSourceFile() {
        return connection.getTempFile(SOURCE_FILE_NAME);
    }

    private OverthereFile getRemoteTempDestinationFile() {
        return connection.getTempFile(SOURCE_FILE_NAME);
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
        return connection.getFile(connection.getTempFile(SOURCE_DIR_NAME).getPath());
    }

    private OverthereFile getRemoteDestinationDirectory() {
        return connection.getFile(connection.getTempFile(SOURCE_DIR_NAME).getPath());
    }

    private OverthereFile getRemoteTempSourceDirectory() {
        return connection.getTempFile(SOURCE_DIR_NAME);
    }

    private OverthereFile getRemoteTempDestinationDirectory() {
        return connection.getTempFile(SOURCE_DIR_NAME);
    }

    private OverthereFile getRemoteDestinationDirectoryWithDifferentName() {
        return connection.getFile(connection.getTempFile(DESTINATION_DIR_ALTERNATIVE_NAME).getPath());
    }

    private OverthereFile appendFileSeparator(OverthereFile dir) {
        return connection.getFile(dir.getPath() + connection.getHostOperatingSystem().getFileSeparator());
    }

    private void shouldCopyToNonExistentFile(final OverthereFile srcFile, final OverthereFile dstFile) {
        populateSourceFile(srcFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    private void shouldCopyToExistentFile(final OverthereFile srcFile, final OverthereFile dstFile) {
        populateSourceFile(srcFile);
        populateExistentDestinationFile(dstFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    private void shouldCopyToNonExistentDirectory(final OverthereFile srcDir, final OverthereFile dstDir) {
        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    private void shouldCopyToExistentDirectory(final OverthereFile srcDir, final OverthereFile dstDir) {
        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
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
