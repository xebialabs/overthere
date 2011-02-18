/*
 * Copyright (c) 2008-2010 XebiaLabs B.V. All rights reserved.
 *
 * Your use of XebiaLabs Software and Documentation is subject to the Personal
 * License Agreement.
 *
 * http://www.xebialabs.com/deployit-personal-edition-license-agreement
 *
 * You are granted a personal license (i) to use the Software for your own
 * personal purposes which may be used in a production environment and/or (ii)
 * to use the Documentation to develop your own plugins to the Software.
 * "Documentation" means the how to's and instructions (instruction videos)
 * provided with the Software and/or available on the XebiaLabs website or other
 * websites as well as the provided API documentation, tutorial and access to
 * the source code of the XebiaLabs plugins. You agree not to (i) lease, rent
 * or sublicense the Software or Documentation to any third party, or otherwise
 * use it except as permitted in this agreement; (ii) reverse engineer,
 * decompile, disassemble, or otherwise attempt to determine source code or
 * protocols from the Software, and/or to (iii) copy the Software or
 * Documentation (which includes the source code of the XebiaLabs plugins). You
 * shall not create or attempt to create any derivative works from the Software
 * except and only to the extent permitted by law. You will preserve XebiaLabs'
 * copyright and legal notices on the Software and Documentation. XebiaLabs
 * retains all rights not expressly granted to You in the Personal License
 * Agreement.
 */

package com.xebialabs.overthere;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains a number of convenience methods for working with HostFile objects.
 */
public class HostFileUtils {

	private static final String ZIP_PATH_SEPARATOR = "/";

	private static final byte[] UTF16LE_PREAMABLE = {(byte) 0xFF, (byte) 0xFE};
	private static final byte[] UTF16BE_PREAMABLE = {(byte) 0xFE, (byte) 0xFF};

	/**
	 * Copies a file or directory.
	 *
	 * @param src the source file or directory.
	 * @param dst the destination file or directory. If it exists it must be of the same type as the source. Its parent directory must exist.
	 * @throws RuntimeIOException if an I/O error occurred
	 */
	public static void copy(HostFile src, HostFile dst) {
		copy(src, dst, null);
	}

	/**
	 * Copies a file or directory.
	 *
	 * @param src         the source file or directory.
	 * @param dst         the destination file or directory. If it exists it must be of the same type as the source. Its parent directory must exist.
	 * @param transformer Transforms the inputstream of the sourcefile, can supply null
	 * @throws RuntimeIOException if an I/O error occurred
	 */
	public static void copy(HostFile src, HostFile dst, HostFileInputStreamTransformer transformer) {
		if (src.isDirectory()) {
			copyDirectory(src, dst, transformer);
		} else {
			copyFile(src, dst, transformer);
		}
	}

	/**
	 * Copies a regular file.
	 *
	 * @param srcFile the source file. Must exists and must not be a directory.
	 * @param dstFile the destination file. May exists but must not be a directory. Its parent directory must exist.
	 * @throws RuntimeIOException if an I/O error occurred
	 */
	public static void copyFile(HostFile srcFile, HostFile dstFile) throws RuntimeIOException {
		copyFile(srcFile, dstFile, null);

	}

	/**
	 * Copies a regular file.
	 *
	 * @param srcFile     the source file. Must exists and must not be a directory.
	 * @param dstFile     the destination file. May exists but must not be a directory. Its parent directory must exist.
	 * @param transformer Transforms the inputstream of the sourcefile, can be null
	 * @throws RuntimeIOException if an I/O error occurred
	 */
	public static void copyFile(HostFile srcFile, HostFile dstFile, HostFileInputStreamTransformer transformer) throws RuntimeIOException {
		// check source file
		if (!srcFile.exists()) {
			throw new RuntimeIOException("Source file " + srcFile + " does not exist");
		}
		if (srcFile.isDirectory()) {
			throw new RuntimeIOException("Source file " + srcFile + " exists but is a directory");
		}

		// check destination file
		if (dstFile.exists()) {
			if (dstFile.isDirectory()) {
				throw new RuntimeIOException("Destination file " + dstFile + " exists but is a directory");
			}
			if (logger.isDebugEnabled())
				logger.debug("About to overwrite existing file " + dstFile);
		}

		if (logger.isDebugEnabled())
			logger.debug("Copying contents of regular file " + srcFile + " to " + dstFile);

		InputStream in = null;
		long length = -1;

		if (transformer != null) {
			InputStream transformedInputStream = transformer.transform(srcFile);
			if (transformedInputStream != null) {
				try {
					ByteArrayOutputStream bytes = readIntoByteArrayOutputStream(srcFile, transformedInputStream);
					length = bytes.size();
					in = new ByteArrayInputStream(bytes.toByteArray());
				} finally {
					//Need to close the input stream
					IOUtils.closeQuietly(transformedInputStream);
				}
			}
		}

		if (in == null) {
			in = srcFile.get();
			length = srcFile.length();
		}

		try {
			dstFile.put(in, length);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Copies a directory recursively.
	 *
	 * @param srcDir the source directory. Must exist and must not be a directory.
	 * @param dstDir the destination directory. May exists but must a directory. Its parent directory must exist.
	 * @throws RuntimeIOException if an I/O error occurred
	 */
	public static void copyDirectory(HostFile srcDir, HostFile dstDir) throws RuntimeIOException {
		copyDirectory(srcDir, dstDir, null);

	}

	/**
	 * Copies a directory recursively.
	 *
	 * @param srcDir      the source directory. Must exist and must not be a directory.
	 * @param dstDir      the destination directory. May exists but must a directory. Its parent directory must exist.
	 * @param transformer Transforms the inputstream of the sourcefile, can be null
	 * @throws RuntimeIOException if an I/O error occurred
	 */
	public static void copyDirectory(HostFile srcDir, HostFile dstDir, HostFileInputStreamTransformer transformer) throws RuntimeIOException {
		// check source directory
		if (!srcDir.exists()) {
			throw new RuntimeIOException("Source directory " + srcDir + " does not exist");
		}
		if (!srcDir.isDirectory()) {
			throw new RuntimeIOException("Source directory " + srcDir + " exists but is not a directory");
		}

		// check destination directory, and create if it does not exist yet
		if (dstDir.exists()) {
			if (!dstDir.isDirectory()) {
				throw new RuntimeIOException("Destination directory " + dstDir + " already exists and is not a directory");
			}
			if (logger.isDebugEnabled())
				logger.debug("About to copy files into existing directory " + dstDir);
		} else {
			if (logger.isDebugEnabled())
				logger.debug("Creating destination directory " + dstDir);
			dstDir.mkdir();
		}

		if (logger.isDebugEnabled())
			logger.debug("Copying contents of directory " + srcDir + " to " + dstDir);
		List<HostFile> srcFiles = srcDir.listFiles();
		for (HostFile srcFile : srcFiles) {
			HostFile dstFile = dstDir.getFile(srcFile.getName());
			copy(srcFile, dstFile, transformer);
		}
	}

	/**
	 * Copies the contents of a string to a {@link HostFile}.
	 *
	 * @param sourceString the string to copy.
	 * @param targetFile   the host file to copy to.
	 */
	public static void putStringToHostFile(String sourceString, HostFile targetFile) {
		byte[] bytes;
		try {
			bytes = sourceString.getBytes("UTF-8");
		} catch (UnsupportedEncodingException exc) {
			throw new RuntimeIOException(exc);
		}

		InputStream in = new ByteArrayInputStream(bytes);
		try {
			targetFile.put(in, bytes.length);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Reads the contents of a {@link HostFile} as a string.
	 *
	 * @param sourceHostFile the host file to read.
	 * @return the contents of the host file.
	 */
	public static String getHostFileAsString(HostFile sourceHostFile) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			sourceHostFile.get(bos);
			byte[] contentBytes = bos.toByteArray();
			return new String(contentBytes, guessCharset(contentBytes));
		} catch (UnsupportedEncodingException exc) {
			throw new RuntimeIOException(exc);
		} finally {
			IOUtils.closeQuietly(bos);
		}
	}

	/**
	 * Guess the character encoding of a byte array by looking at its bytes. If the first two bytes are a Unicode byte order mark, the bytes are assumed to be
	 * in UTF-16 encoding (depending on the values of the bytes). Otherwise the encoding is assumed to be UTF-8.
	 *
	 * @param rawContent the bytes to inspect
	 * @return the character encoding we've guessed the bytes are in.
	 */
	private static String guessCharset(byte[] rawContent) {
		byte[] rawContentFirstBytes = ArrayUtils.subarray(rawContent, 0, 2);

		boolean looksLikeUTF16LE = Arrays.equals(rawContentFirstBytes, UTF16LE_PREAMABLE);
		boolean looksLikeUTF16BE = Arrays.equals(rawContentFirstBytes, UTF16BE_PREAMABLE);
		return ((looksLikeUTF16LE || looksLikeUTF16BE) ? "UTF-16" : "UTF-8");
	}

	/**
	 * Unzips a host file.
	 *
	 * @param zip the file to unzip.
	 * @param dir the directory to unzip to.
	 */
	public static void unzip(HostFile zip, HostFile dir) {
		unzip(zip, dir, null);
	}

	public static void unzip(HostFile zip, HostFile dir, HostFileInputStreamTransformer transformer) {
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir + " is not a directory");
		}
		if (transformer == null) {
			unzip(zip.getPath(), zip.get(), dir);
		} else {
			unzip(zip.getPath(),transformer.transform(zip), dir);
		}
	}

	private static void unzip(String zipPath, InputStream zipStream, HostFile dstDir) {
		if (logger.isDebugEnabled())
			logger.debug("Unzipping " + zipPath + " to " + dstDir);

		ZipInputStream zipIn = new ZipInputStream(zipStream);
		try {
			try {
				ZipEntry entry;
				while ((entry = zipIn.getNextEntry()) != null) {
					extractZipEntry(zipIn, entry, dstDir);
				}
			} catch (IOException exc) {
				throw new RuntimeIOException("Cannot unzip " + zipPath + " to " + dstDir, exc);
			}
		} finally {
			IOUtils.closeQuietly(zipIn);
		}
	}

	private static void extractZipEntry(ZipInputStream zipIn, ZipEntry entry, HostFile dir) {
		String[] pathElements = StringUtils.split(entry.getName(), ZIP_PATH_SEPARATOR);
		String dstFileSeparator = dir.getConnection().getHostOperatingSystem().getFileSeparator();
		String dstPath = StringUtils.join(pathElements, dstFileSeparator);
		HostFile dstFile = dir.getFile(dstPath);

		if (logger.isDebugEnabled())
			logger.debug("Unzipping " + entry.getName() + " to " + dstFile.getPath());

		if (entry.isDirectory()) {
			dstFile.mkdirs();
		} else {
			dstFile.getParentFile().mkdirs();
			dstFile.put(zipIn, entry.getSize());
		}
	}

	private static ByteArrayOutputStream readIntoByteArrayOutputStream(HostFile srcFile, InputStream inputStream) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		int value;
		try {
			while ((value = inputStream.read()) != -1) {
				outputStream.write(value);
			}
		} catch (IOException e) {
			throw new RuntimeIOException("Unable to read bytes from Inputstream for file " + srcFile, e);
		}
		return outputStream;
	}

	private static Logger logger = LoggerFactory.getLogger(HostFileUtils.class);

}
