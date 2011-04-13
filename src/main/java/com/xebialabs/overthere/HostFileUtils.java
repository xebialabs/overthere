/*
 * This file is part of Overthere.
 * 
 * Overthere is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Overthere is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Overthere.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains a number of convenience methods for working with HostFile objects.
 */
public class HostFileUtils {

	private static final String ZIP_PATH_SEPARATOR = "/";

	/**
	 * Copies a file or directory.
	 *
	 * @param src the source file or directory.
	 * @param dst the destination file or directory. If it exists it must be of the same type as the source. Its parent directory must exist.
	 * @throws RuntimeIOException if an I/O error occurred
	 */
	public static void copy(OverthereFile src, OverthereFile dst) {
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
	public static void copy(OverthereFile src, OverthereFile dst, HostFileInputStreamTransformer transformer) {
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
	public static void copyFile(OverthereFile srcFile, OverthereFile dstFile) throws RuntimeIOException {
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
	public static void copyFile(OverthereFile srcFile, OverthereFile dstFile, HostFileInputStreamTransformer transformer) throws RuntimeIOException {
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
					closeQuietly(transformedInputStream);
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
			closeQuietly(in);
		}
	}

	/**
	 * Copies a directory recursively.
	 *
	 * @param srcDir the source directory. Must exist and must not be a directory.
	 * @param dstDir the destination directory. May exists but must a directory. Its parent directory must exist.
	 * @throws RuntimeIOException if an I/O error occurred
	 */
	public static void copyDirectory(OverthereFile srcDir, OverthereFile dstDir) throws RuntimeIOException {
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
	public static void copyDirectory(OverthereFile srcDir, OverthereFile dstDir, HostFileInputStreamTransformer transformer) throws RuntimeIOException {
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
		OverthereFile[] srcFiles = srcDir.listFiles();
		for (OverthereFile srcFile : srcFiles) {
			OverthereFile dstFile = dstDir.getFile(srcFile.getName());
			copy(srcFile, dstFile, transformer);
		}
	}

	/**
	 * Copies the contents of a string to a {@link OverthereFile}.
	 *
	 * @param sourceString the string to copy.
	 * @param targetFile   the host file to copy to.
	 */
	public static void putStringToHostFile(String sourceString, OverthereFile targetFile) {
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
			closeQuietly(in);
		}
	}

	/**
	 * Unzips a host file.
	 *
	 * @param zip the file to unzip.
	 * @param dir the directory to unzip to.
	 */
	public static void unzip(OverthereFile zip, OverthereFile dir) {
		unzip(zip, dir, null);
	}

	public static void unzip(OverthereFile zip, OverthereFile dir, HostFileInputStreamTransformer transformer) {
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir + " is not a directory");
		}
		if (transformer == null) {
			unzip(zip.getPath(), zip.get(), dir);
		} else {
			unzip(zip.getPath(),transformer.transform(zip), dir);
		}
	}

	private static void unzip(String zipPath, InputStream zipStream, OverthereFile dstDir) {
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
			closeQuietly(zipIn);
		}
	}

	private static void extractZipEntry(ZipInputStream zipIn, ZipEntry entry, OverthereFile dir) {
		String[] pathElements = StringUtils.split(entry.getName(), ZIP_PATH_SEPARATOR);
		String dstFileSeparator = dir.getConnection().getHostOperatingSystem().getFileSeparator();
		String dstPath = StringUtils.join(pathElements, dstFileSeparator);
		OverthereFile dstFile = dir.getFile(dstPath);

		if (logger.isDebugEnabled())
			logger.debug("Unzipping " + entry.getName() + " to " + dstFile.getPath());

		if (entry.isDirectory()) {
			dstFile.mkdirs();
		} else {
			dstFile.getParentFile().mkdirs();
			dstFile.put(zipIn, entry.getSize());
		}
	}

	private static ByteArrayOutputStream readIntoByteArrayOutputStream(OverthereFile srcFile, InputStream inputStream) {
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

