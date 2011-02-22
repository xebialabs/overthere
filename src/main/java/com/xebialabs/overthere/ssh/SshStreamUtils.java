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
package com.xebialabs.overthere.ssh;

import java.io.IOException;
import java.io.InputStream;

import com.xebialabs.overthere.RuntimeIOException;

/**
 * Contains a number of utility methods to handle SSH streams.
 */
class SshStreamUtils {

	static int checkAck(InputStream in) throws RuntimeIOException {
		try {
			int b = in.read();
			// b may be
			// -1 for EOF,
			// 0 for success,
			// 1 for error,
			// 2 for fatal error,
			// or any other character
			if (b == 0) {
				return b;
			} else if (b == -1) {
				throw new RuntimeIOException("End-of-file reached while reading from SCP stream");
			} else if (b == 1 || b == 2) {
				StringBuffer sb = new StringBuffer();
				int c;
				do {
					c = in.read();
					sb.append((char) c);
				} while (c != '\n');
				if (b == 1) {
					throw new RuntimeIOException("Error received from SCP stream: " + sb);
				}
				if (b == 2) { // fatal error
					throw new RuntimeIOException("Fatal error received from SCP stream: " + sb);
				}
			}
			return b;
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}
	}

}

