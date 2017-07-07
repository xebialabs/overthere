package com.xebialabs.overthere.util;

import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static com.xebialabs.overthere.util.OverthereUtils.write;

public class OverthereFileTranscoder extends OverthereFileTransmitter {
    private static final String SOURCE = "Source";
    private static final String DESTINATION = "Destination";
    private final Charset srcCharset;
    private final Charset dstCharset;

    public OverthereFileTranscoder(Charset srcCharset, Charset dstCharset) {
        this.srcCharset = srcCharset;
        this.dstCharset = dstCharset;
    }

    private OverthereFileTranscoder(OverthereFile source, Charset srcCharset, OverthereFile dest, Charset dstCharset) {
        super(source, dest);
        this.srcCharset = srcCharset;
        this.dstCharset = dstCharset;
    }

    /**
     * Transcode a file or directory using the default configured character encodings for the respective {@link OverthereFile}s.
     *
     * @param src the source file or directory.
     * @param dst the destination file or directory. If it exists it must be of the same type as the source. Its parent
     *            directory must exist.
     * @throws RuntimeIOException if an I/O error occurred
     */
    public static void transcode(OverthereFile src, OverthereFile dst) {
        src.getConnection().getOptions().get()
        if (src.isDirectory()) {
            transcodeDirectory(src, dst);
        } else {
            transcodeFile(src, dst);
        }
    }

    /**
     * Transcode a file or directory using the provided character encodings.
     *
     * @param src the source file or directory.
     * @param srcCharsetName the charset name of the source file(s).
     * @param dst the destination file or directory. If it exists it must be of the same type as the source. Its parent
     *            directory must exist.
     * @param dstCharsetName the charset name of the destination file(s).
     * @throws RuntimeIOException if an I/O error occurred
     */
    public static void transcode(OverthereFile src, String srcCharsetName, OverthereFile dst, String dstCharsetName) {
        if (src.isDirectory()) {
            transcodeDirectory(src, dst);
        } else {
            transcodeFile(src, dst);
        }
    }

    /**
     * Transcode a file or directory using the provided character encodings.
     *
     * @param src the source file or directory.
     * @param srcCharset the charset of the source file(s).
     * @param dst the destination file or directory. If it exists it must be of the same type as the source. Its parent
     *            directory must exist.
     * @param dstCharset the charset of the destination file(s).
     * @throws RuntimeIOException if an I/O error occurred
     */
    public static void transcode(OverthereFile src, Charset srcCharset, OverthereFile dst, Charset dstCharset) {
        if (src.isDirectory()) {
            transcodeDirectory(src, dst);
        } else {
            transcodeFile(src, dst);
        }
    }

    /**
     * Copies a directory recursively.
     *
     * @param srcDir the source directory. Must exist and must not be a directory.
     * @param dstDir the destination directory. May exists but must a directory. Its parent directory must exist.
     * @throws RuntimeIOException if an I/O error occurred
     */
    private static void transcodeDirectory(OverthereFile srcDir, Charset srcCharset, OverthereFile dstDir, Charset dstCharset) throws RuntimeIOException {
        OverthereFileTranscoder dirCopier = new OverthereFileTranscoder(srcDir, srcCharset, dstDir, dstCharset);
        dirCopier.startCopy();
    }

    /**
     * Copies a regular file.
     *
     * @param srcFile the source file. Must exists and must not be a directory.
     * @param dstFile the destination file. May exists but must not be a directory. Its parent directory must exist.
     * @throws RuntimeIOException if an I/O error occurred
     */
    private static void transcodeFile(final OverthereFile srcFile, final OverthereFile dstFile) throws RuntimeIOException {
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

    @Override
    protected void transmitFile(OverthereFile srcFile, OverthereFile dstFile) {

    }
}
