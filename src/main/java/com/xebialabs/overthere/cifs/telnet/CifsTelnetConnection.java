/*
 * Copyright (c) 2008-2014, XebiaLabs B.V., All rights reserved.
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
package com.xebialabs.overthere.cifs.telnet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.WindowSizeOptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.io.Closeables;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.cifs.CifsConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static java.lang.String.format;

/**
 * A connection to a Windows host using CIFS and Telnet.
 * <p/>
 * Limitations:
 * <ul>
 * <li>Shares with names like C$ need to available for all drives accessed. In practice, this means that Administrator
 * access is needed.</li>
 * <li>Windows Telnet Service must be configured to use stream mode:<br/>
 * <tt>&gt; tlntadmn config mode=stream</tt></li>
 * <li>Not tested with domain accounts.</li>
 * </ul>
 */
public class CifsTelnetConnection extends CifsConnection {

    private static final String DETECTABLE_WINDOWS_PROMPT = "TELNET4OVERTHERE ";

    private static final String ERRORLEVEL_PREAMBLE = "ERRORLEVEL-PREAMBLE";

    private static final String ERRORLEVEL_POSTAMBLE = "ERRORLEVEL-POSTAMBLE";

    /**
     * Creates a {@link CifsTelnetConnection}. Don't invoke directly. Use
     * {@link Overthere#getConnection(String, ConnectionOptions)} instead.
     */
    public CifsTelnetConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper, true);
        checkArgument(os == WINDOWS, "Cannot start a " + CIFS_PROTOCOL + ":%s connection to a host that is not running Windows", cifsConnectionType.toString().toLowerCase());
        checkArgument(!username.contains("@"), "Cannot start a " + CIFS_PROTOCOL + ":%s connection with a new-style Windows domain account [%s], use DOMAIN\\USER instead.", cifsConnectionType.toString().toLowerCase(), username);
    }

    @Override
    public OverthereProcess startProcess(final CmdLine cmd) {
        checkNotNull(cmd, "Cannot execute null command line");
        checkArgument(cmd.getArguments().size() > 0, "Cannot execute empty command line");

        final String obfuscatedCmd = cmd.toCommandLine(os, true);
        logger.info("Starting command [{}] on [{}]", obfuscatedCmd, this);

        try {
            final TelnetClient tc = new TelnetClient();
            tc.setConnectTimeout(connectionTimeoutMillis);
            tc.addOptionHandler(new WindowSizeOptionHandler(299, 25, true, false, true, false));
            logger.info("Connecting to telnet://{}@{}", username, address);
            tc.connect(address, port);
            final InputStream stdout = tc.getInputStream();
            final OutputStream stdin = tc.getOutputStream();
            final PipedInputStream callersStdout = new PipedInputStream();
            final PipedOutputStream toCallersStdout = new PipedOutputStream(callersStdout);
            final ByteArrayOutputStream outputBuf = new ByteArrayOutputStream();
            final int[] exitValue = new int[1];
            exitValue[0] = -1;

            final Thread outputReaderThread = new Thread("Telnet output reader") {
                @Override
                public void run() {
                    try {
                        receive(stdout, outputBuf, toCallersStdout, "ogin:");
                        send(stdin, username);

                        receive(stdout, outputBuf, toCallersStdout, "assword:");
                        send(stdin, password);

                        receive(stdout, outputBuf, toCallersStdout, ">", "ogon failure");
                        send(stdin, "PROMPT " + DETECTABLE_WINDOWS_PROMPT);
                        // We must wait for the prompt twice; the first time is an echo of the PROMPT command,
                        // the second is the actual prompt
                        receive(stdout, outputBuf, toCallersStdout, DETECTABLE_WINDOWS_PROMPT);
                        receive(stdout, outputBuf, toCallersStdout, DETECTABLE_WINDOWS_PROMPT);

                        if (workingDirectory != null) {
                            send(stdin, "CD /D " + workingDirectory.getPath());
                            receive(stdout, outputBuf, toCallersStdout, DETECTABLE_WINDOWS_PROMPT);
                        }

                        send(stdin, cmd.toCommandLine(getHostOperatingSystem(), false));

                        receive(stdout, outputBuf, toCallersStdout, DETECTABLE_WINDOWS_PROMPT);

                        send(stdin, "ECHO \"" + ERRORLEVEL_PREAMBLE + "%errorlevel%" + ERRORLEVEL_POSTAMBLE);
                        receive(stdout, outputBuf, toCallersStdout, ERRORLEVEL_POSTAMBLE);
                        receive(stdout, outputBuf, toCallersStdout, ERRORLEVEL_POSTAMBLE);
                        String outputBufStr = outputBuf.toString();
                        int preamblePos = outputBufStr.indexOf(ERRORLEVEL_PREAMBLE);
                        int postamblePos = outputBufStr.indexOf(ERRORLEVEL_POSTAMBLE);
                        if (preamblePos >= 0 && postamblePos >= 0) {
                            String errorlevelString = outputBufStr.substring(preamblePos + ERRORLEVEL_PREAMBLE.length(), postamblePos);
                            logger.debug("Errorlevel string found: {}", errorlevelString);

                            try {
                                synchronized (exitValue) {
                                    exitValue[0] = Integer.parseInt(errorlevelString);
                                }
                            } catch (NumberFormatException exc) {
                                logger.error("Cannot parse errorlevel in Windows output: " + outputBuf);
                            }
                        } else {
                            logger.error("Cannot find errorlevel in Windows output: " + outputBuf);
                        }
                    } catch (IOException exc) {
                        throw new RuntimeIOException(format("Cannot start command [%s] on [%s]", obfuscatedCmd, CifsTelnetConnection.this), exc);
                    } finally {
                        Closeables.closeQuietly(toCallersStdout);
                    }
                }
            };
            outputReaderThread.setDaemon(true);
            outputReaderThread.start();

            return new OverthereProcess() {
                @Override
                public synchronized OutputStream getStdin() {
                    return stdin;
                }

                @Override
                public synchronized InputStream getStdout() {
                    return callersStdout;
                }

                @Override
                public synchronized InputStream getStderr() {
                    return new ByteArrayInputStream(new byte[0]);
                }

                @Override
                public synchronized int waitFor() {
                    if (!tc.isConnected()) {
                        return exitValue[0];
                    }

                    try {
                        try {
                            outputReaderThread.join();
                        } finally {
                            disconnect();
                        }
                        return exitValue[0];
                    } catch (InterruptedException exc) {
                        throw new RuntimeIOException(format("Cannot start command [%s] on [%s]", obfuscatedCmd, CifsTelnetConnection.this), exc);
                    }
                }

                @Override
                public synchronized void destroy() {
                    if (!tc.isConnected()) {
                        return;
                    }

                    disconnect();
                }

                private synchronized void disconnect() {
                    try {
                        tc.disconnect();
                        logger.info("Disconnected from {}", CifsTelnetConnection.this);

                        Closeables.closeQuietly(toCallersStdout);
                    } catch (IOException exc) {
                        throw new RuntimeIOException(format("Cannot disconnect from %s", CifsTelnetConnection.this), exc);
                    }
                }

                @Override
                public synchronized int exitValue() {
                    if (tc.isConnected()) {
                        throw new IllegalThreadStateException(format("Process for command [%s] on %s is still running", obfuscatedCmd, CifsTelnetConnection.this));
                    }

                    synchronized (exitValue) {
                        return exitValue[0];
                    }
                }
            };
        } catch (InvalidTelnetOptionException exc) {
            throw new RuntimeIOException("Cannot execute command " + cmd + " at telnet://" + username + "@" + address, exc);
        } catch (IOException exc) {
            throw new RuntimeIOException("Cannot execute command " + cmd + " at telnet://" + username + "@" + address, exc);
        }
    }

    private static void receive(final InputStream stdout, final ByteArrayOutputStream outputBuf, final PipedOutputStream toCallersStdout,
                                final String expectedString) throws IOException {
        receive(stdout, outputBuf, toCallersStdout, expectedString, null);
    }

    private static void receive(final InputStream stdout, final ByteArrayOutputStream outputBuf, final PipedOutputStream toCallersStdout,
                                final String expectedString, final String unexpectedString) throws IOException {
        boolean lastCharWasCr = false;
        boolean lastCharWasEsc = false;
        for (; ; ) {
            int cInt = stdout.read();
            if (cInt == -1) {
                throw new IOException("End of stream reached");
            }

            outputBuf.write(cInt);
            final String outputBufStr = outputBuf.toString();
            char c = (char) cInt;
            switch (c) {
                case '\r':
                    handleReceivedLine(outputBuf, outputBufStr, toCallersStdout);
                    break;
                case '\n':
                    if (!lastCharWasCr) {
                        handleReceivedLine(outputBuf, outputBufStr, toCallersStdout);
                    }
                    break;
                case '[':
                    if (lastCharWasEsc) {
                        throw new RuntimeIOException(
                                "VT100/ANSI escape sequence found in output stream. Please configure the Windows Telnet server to use stream mode (tlntadmn config mode=stream).");
                    }
            }
            lastCharWasCr = (c == '\r');
            lastCharWasEsc = (c == 27);

            if (unexpectedString != null && outputBufStr.length() >= unexpectedString.length()) {
                String s = outputBufStr.substring(outputBufStr.length() - unexpectedString.length(), outputBufStr.length());
                if (s.equals(unexpectedString)) {
                    logger.debug("Unexpected string [{}] found in Windows Telnet output", unexpectedString);
                    throw new IOException(format("Unexpected string [%s] found in Windows Telnet output", unexpectedString));
                }
            }

            if (outputBufStr.length() >= expectedString.length()) {
                String s = outputBufStr.substring(outputBufStr.length() - expectedString.length(), outputBufStr.length());
                if (s.equals(expectedString)) {
                    logger.debug("Expected string [{}] found in Windows Telnet output", expectedString);
                    return;
                }
            }
        }
    }

    private static void handleReceivedLine(final ByteArrayOutputStream outputBuf, final String outputBufStr, final PipedOutputStream toCallersStdout)
            throws IOException {
        if (!outputBufStr.contains(DETECTABLE_WINDOWS_PROMPT)) {
            toCallersStdout.write(outputBuf.toByteArray());
            toCallersStdout.flush();
        }
        outputBuf.reset();
    }

    private static void send(final OutputStream stdin, final String lineToSend) throws IOException {
        byte[] bytesToSend = (lineToSend + "\r\n").getBytes();
        stdin.write(bytesToSend);
        stdin.flush();
    }

    private static Logger logger = LoggerFactory.getLogger(CifsTelnetConnection.class);
}
