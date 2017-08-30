package com.xebialabs.overthere.util;

import com.xebialabs.overthere.*;

import java.io.*;
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
     * Transcode a String with teh given encoding to the configured character encoding of the target {@link OverthereFile}.
     */
    public static void transcode(String contents, String srcCharsetName, OverthereFile dst) {
        Charset srcCharset = Charset.forName(srcCharsetName);
        Charset dstCharset = getConfiguredCharacterSet(dst.getConnection());
        OverthereFile baf = new ByteArrayFile("byte_array", contents.getBytes(srcCharset));
        transcode(baf, srcCharset, dst, dstCharset);
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
        Charset srcCharset = getConfiguredCharacterSet(src.getConnection());
        Charset dstCharset = getConfiguredCharacterSet(dst.getConnection());
        transcode(src, srcCharset, dst, dstCharset);
    }

    private static Charset getConfiguredCharacterSet(OverthereConnection connection) {
        OperatingSystemFamily hostOperatingSystem = connection.getHostOperatingSystem();
        String charsetName = connection.getOptions().get(ConnectionOptions.REMOTE_CHARACTER_ENCODING, hostOperatingSystem.getDefaultCharacterSet());
        return Charset.forName(charsetName);
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
        Charset srcCharset = Charset.forName(srcCharsetName);
        Charset dstCharset = Charset.forName(dstCharsetName);
        transcode(src, srcCharset, dst, dstCharset);
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
            transcodeDirectory(src, srcCharset, dst, dstCharset);
        } else {
            new OverthereFileTranscoder(srcCharset, dstCharset).transmitFile(src, dst);
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
        dirCopier.startTransmission();
    }

    @Override
    protected void transmitFile(OverthereFile srcFile, OverthereFile dstFile) {
        checkFileExists(srcFile, SOURCE);
        checkReallyIsAFile(dstFile, DESTINATION);

        logger.debug("Transcoding file {} ({}) to {} ({})", srcFile, srcCharset, dstFile, dstCharset);
        if (dstFile.exists()) {
            logger.trace("About to overwrite existing file {}", dstFile);
        }

        try {
            Reader in = new BufferedReader(new InputStreamReader(srcFile.getInputStream(), srcCharset));
            try {
                Writer out = new BufferedWriter(new OutputStreamWriter(dstFile.getOutputStream(), dstCharset));
                try {
                    write(in, out);
                } finally {
                    closeQuietly(out);
                }
            } finally {
                closeQuietly(in);
            }
        } catch (RuntimeIOException exc) {
            throw new RuntimeIOException("Cannot transcode " + srcFile + " (" + srcCharset + ") to " + dstFile + " (" + dstCharset + ")", exc.getCause());
        }
    }
}
