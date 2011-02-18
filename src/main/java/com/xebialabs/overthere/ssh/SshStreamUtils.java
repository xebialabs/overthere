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

package com.xebialabs.overthere.ssh;

import java.io.IOException;
import java.io.InputStream;

import com.xebialabs.deployit.exception.RuntimeIOException;

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
