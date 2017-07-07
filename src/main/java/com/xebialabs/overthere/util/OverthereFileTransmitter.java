package com.xebialabs.overthere.util;

import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Stack;

public abstract class OverthereFileTransmitter extends OverthereFileDirectoryWalker {
    private static final String SOURCE = "Source";
    private static final String DESTINATION = "Destination";

    private Stack<OverthereFile> dstDirStack = new Stack<OverthereFile>();
    private OverthereFile srcDir;

    protected OverthereFileTransmitter() {
    }

    protected OverthereFileTransmitter(OverthereFile srcDir, OverthereFile dstDir) {
        dstDirStack.push(dstDir);
        this.srcDir = srcDir;
        checkDirectoryExists(srcDir, SOURCE);
    }



    @Override
    protected void handleDirectoryStart(OverthereFile scrDir, int depth) throws IOException {
        OverthereFile dstDir = getCurrentDestinationDir();
        if (depth != ROOT) {
            dstDir = createSubdirectoryAndMakeCurrent(dstDir, scrDir.getName());
        }

        if (dstDir.exists()) {
            checkReallyIsADirectory(dstDir, DESTINATION);
            logger.trace("About to copy files into existing directory {}", dstDir);
        } else {
            dstDir.mkdir();
        }
    }

    private OverthereFile createSubdirectoryAndMakeCurrent(OverthereFile parentDir, String subdirName) {
        OverthereFile subdir = parentDir.getFile(subdirName);
        dstDirStack.push(subdir);
        return subdir;
    }

    protected void startTransmission() {
        walk(srcDir);
    }

    private OverthereFile getCurrentDestinationDir() {
        return dstDirStack.peek();
    }

    @Override
    protected void handleFile(OverthereFile srcFile, int depth) throws IOException {
        OverthereFile dstFile = getCurrentDestinationDir().getFile(srcFile.getName());
        transmitFile(srcFile, dstFile);
    }

    protected abstract void transmitFile(OverthereFile srcFile, OverthereFile dstFile);

    @Override
    protected void handleDirectoryEnd(OverthereFile directory, int depth) throws IOException {
        if (depth != ROOT) {
            dstDirStack.pop();
        }
    }

    /**
     * Assert that if a file exists, it is not a directory.
     *
     * @param file            to check.
     * @param fileDescription to prepend to error message.
     * @throws RuntimeIOException if file is a directory.
     */
    protected void checkReallyIsAFile(OverthereFile file, String fileDescription) {
        if (file.exists() && file.isDirectory()) {
            throw new RuntimeIOException(fileDescription + " file " + file + " exists but is a directory");
        }
    }

    /**
     * Assert that the directory exists.
     *
     * @param dir            is the directory to check.
     * @param dirDescription to prepend to error message.
     * @throws RuntimeIOException if directory does not exist or if it a flat file.
     */
    protected void checkDirectoryExists(OverthereFile dir, String dirDescription) {
        if (!dir.exists()) {
            throw new RuntimeIOException(dirDescription + " directory " + dir + " does not exist");
        }
        checkReallyIsADirectory(dir, dirDescription);
    }

    /**
     * Assert that if a file exists, it must be a directory.
     *
     * @param dir            is the directory to check.
     * @param dirDescription to prepend to error message.
     * @throws RuntimeIOException if file is not a directory.
     */
    protected void checkReallyIsADirectory(OverthereFile dir, String dirDescription) {
        if (dir.exists() && !dir.isDirectory()) {
            throw new RuntimeIOException(dirDescription + " directory " + dir + " exists but is not a directory");
        }
    }

    /**
     * Assert that the file must exist and it is not a directory.
     *
     * @param file              to check.
     * @param sourceDescription to prepend to error message.
     * @throws RuntimeIOException if file does not exist or is a directory.
     */
    protected void checkFileExists(OverthereFile file, String sourceDescription) {
        if (!file.exists()) {
            throw new RuntimeIOException(sourceDescription + " file " + file + " does not exist");
        }
        checkReallyIsAFile(file, sourceDescription);
    }

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
}
