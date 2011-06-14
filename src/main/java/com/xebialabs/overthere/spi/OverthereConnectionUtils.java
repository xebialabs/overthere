package com.xebialabs.overthere.spi;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;

/**
 * Contains utility methods for the implementations of {@link OverthereConnection}. Not for part of the public Overthere API.
 */
public class OverthereConnectionUtils {

	/**
	 * Holds results of an ls call
	 */
	public static class LsResults {
		public boolean exists;
		public boolean isDirectory;
		public long length = -1;

		public boolean canRead;
		public boolean canWrite;
		public boolean canExecute;
	}

	/**
	 * Gets information about the file by executing "ls -ld" on it.
	 * 
	 * @param file
	 *            the file
	 * @return the information about the file, never <code>null</code>.
	 * @throws RuntimeIOException
	 *             if an I/O exception occurs
	 */
	public static LsResults getFileInfo(OverthereFile file) throws RuntimeIOException {
		LsResults results = new LsResults();
		CapturingOverthereProcessOutputHandler capturedOutput = new CapturingOverthereProcessOutputHandler();
		int errno = file.getConnection().execute(capturedOutput, CmdLine.build("ls", "-ld", file.getPath()));
		if (errno == 0) {
			results.exists = true;
			if (capturedOutput.getOutputLines().size() > 0) {
				// parse ls results
				String outputLine = capturedOutput.getOutputLines().get(0);
				if (logger.isDebugEnabled())
					logger.debug("ls output = " + outputLine);
				StringTokenizer outputTokens = new StringTokenizer(outputLine);
				if (outputTokens.countTokens() < 5) {
					throw new RuntimeIOException("ls -ld " + file.getPath() + " returned output that contains less than the expected 5 tokens");
				}
				String permissions = outputTokens.nextToken();
				@SuppressWarnings("unused")
				String inodelinkes = outputTokens.nextToken();
				@SuppressWarnings("unused")
				String owner = outputTokens.nextToken();
				@SuppressWarnings("unused")
				String group = outputTokens.nextToken();
				String size = outputTokens.nextToken();

				results.isDirectory = permissions.length() >= 1 && permissions.charAt(0) == 'd';
				results.canRead = permissions.length() >= 2 && permissions.charAt(1) == 'r';
				results.canWrite = permissions.length() >= 3 && permissions.charAt(2) == 'w';
				results.canExecute = permissions.length() >= 4 && permissions.charAt(3) == 'x';
				try {
					results.length = Integer.parseInt(size);
				} catch (NumberFormatException ignore) {
				}
			}
		} else {
			results.exists = false;
		}

		if (logger.isDebugEnabled())
			logger.debug("Listed file " + file + ": exists=" + results.exists + ", isDirectory=" + results.isDirectory + ", length=" + results.length
			        + ", canRead=" + results.canRead + ", canWrite=" + results.canWrite + ", canExecute=" + results.canExecute);
		return results;
	}

	private static Logger logger = LoggerFactory.getLogger(OverthereConnectionUtils.class);

}
