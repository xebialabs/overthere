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
package com.xebialabs.overthere.util;

import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

import static com.xebialabs.overthere.util.OverthereFileDirectoryWalker.ROOT;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static com.xebialabs.overthere.util.OverthereUtils.write;

/**
 * OverthereFile copy utility that uses only the input and output streams exposed by the OverthereFile to perform the
 * copying action.
 * <p/>
 * FIXME: Move to its proper place
 */
public final class OverthereFileCopier extends OverthereFileTransmitter {

    private static final String SOURCE = "Source";
    private static final String DESTINATION = "Destination";

    private Stack<OverthereFile> dstDirStack = new Stack<OverthereFile>();
    private OverthereFile srcDir;

    private OverthereFileCopier() {
    }

    private OverthereFileCopier(OverthereFile srcDir, OverthereFile dstDir) {
        super(srcDir, dstDir);
    }
    /**
     * Copies a file or directory.
     *
     * @param src the source file or directory.
     * @param dst the destination file or directory. If it exists it must be of the same type as the source. Its parent
     *            directory must exist.
     * @throws RuntimeIOException if an I/O error occurred
     */
    public static void copy(OverthereFile src, OverthereFile dst) {
        if (src.isDirectory()) {
            copyDirectory(src, dst);
        } else {
            new OverthereFileCopier().transmitFile(src, dst);
        }
    }

    /**
     * Copies a directory recursively.
     *
     * @param srcDir the source directory. Must exist and must not be a directory.
     * @param dstDir the destination directory. May exists but must a directory. Its parent directory must exist.
     * @throws RuntimeIOException if an I/O error occurred
     */
    private static void copyDirectory(OverthereFile srcDir, OverthereFile dstDir) throws RuntimeIOException {
        OverthereFileCopier dirCopier = new OverthereFileCopier(srcDir, dstDir);
        dirCopier.startTransmission();
    }

    /**
     * Copies a regular file.
     *
     * @param srcFile the source file. Must exists and must not be a directory.
     * @param dstFile the destination file. May exists but must not be a directory. Its parent directory must exist.
     * @throws RuntimeIOException if an I/O error occurred
     */
    @Override
    protected void transmitFile(final OverthereFile srcFile, final OverthereFile dstFile) throws RuntimeIOException {
        checkFileExists(srcFile, SOURCE);
        checkReallyIsAFile(dstFile, DESTINATION);

        logger.debug("Copying file {} to {}", srcFile, dstFile);
        if (dstFile.exists())
            logger.trace("About to overwrite existing file {}", dstFile);

        try {
            InputStream is = srcFile.getInputStream();
            try {
                OutputStream os = dstFile.getOutputStream();
                try {
                    write(is, os);
                } finally {
                    closeQuietly(os);
                }
            } finally {
                closeQuietly(is);
            }
        } catch (RuntimeIOException exc) {
            throw new RuntimeIOException("Cannot copy " + srcFile + " to " + dstFile, exc.getCause());
        }
    }
}
