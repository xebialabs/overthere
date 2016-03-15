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
package com.xebialabs.overthere.ssh;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects password prompts in the output stream and sends the password in response.
 */
class SshElevatedPasswordHandlingStream extends FilterInputStream {
    private final OutputStream remoteStdin;
    private final byte[] passwordBytes;
    private final String passwordRegex;
    private final Pattern passwordPattern;

    private final StringBuilder receivedOutputBuffer = new StringBuilder();

    private boolean sentPassword = false;

    protected SshElevatedPasswordHandlingStream(InputStream remoteStdout, OutputStream remoteStdin, String password, String passwordPromptRegex) {
        super(remoteStdout);
        this.remoteStdin = remoteStdin;
        this.passwordBytes = (password + "\r\n").getBytes();

        this.passwordRegex = passwordPromptRegex;
        this.passwordPattern = Pattern.compile(passwordRegex);
    }

    @Override
    public int read() throws IOException {
        int readInt = super.read();
        if (readInt > -1) {
            handleChar((char) readInt);
        }
        return readInt;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int numBytesRead = super.read(b, off, len);
        if (numBytesRead > -1) {
            for (int i = 0; i < numBytesRead; i++) {
                handleChar((char) b[off + i]);
            }
        }
        return numBytesRead;
    }

    private void handleChar(char c) {
//        if (!sentPassword) {
            logger.trace("Received: {}", c);
            if (c == '\n') {
                receivedOutputBuffer.setLength(0);
            } else {
                receivedOutputBuffer.append(c);

                if (c == passwordRegex.charAt(passwordRegex.length() - 1)) {
                    String receivedOutput = receivedOutputBuffer.toString();
                    if (passwordPattern.matcher(receivedOutput).matches()) {
                        logger.info("Found password prompt in output: {}", receivedOutput);
                        sentPassword = true;
                        try {
                            remoteStdin.write(passwordBytes);
                            remoteStdin.flush();
                            logger.debug("Sent password");
                        } catch (IOException exc) {
                            logger.error("Cannot send password", exc);
                        }
                    }
                }
            }
//        }
    }

    private static Logger logger = LoggerFactory.getLogger(SshElevatedPasswordHandlingStream.class);

}
