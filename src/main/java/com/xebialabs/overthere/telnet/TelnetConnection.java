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
package com.xebialabs.overthere.telnet;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import com.xebialabs.overthere.spi.ProcessConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.WindowSizeOptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.cifs.ConnectionValidator.checkIsWindowsHost;
import static com.xebialabs.overthere.cifs.ConnectionValidator.checkNotNewStyleWindowsDomain;
import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static com.xebialabs.overthere.util.OverthereUtils.checkNotNull;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static java.lang.String.format;
import static java.net.InetSocketAddress.createUnresolved;
/**
 * A connection to a Windows host using Telnet.
 * <p/>
 * Limitations:
 * <ul>
 * <li>Shares with names like C$ need to available for all drives accessed. In practice, this means that Administrator
 * access is needed.</li>
 * <li>Windows Telnet Service must be configured to use stream mode:<br/>
 * <tt>&gt; tlntadmn config mode=stream</tt></li>
 * </ul>
 */
public class TelnetConnection implements ProcessConnection {

    private static final String DETECTABLE_WINDOWS_PROMPT = "TELNET4OVERTHERE ";

    private static final String ERRORLEVEL_PREAMBLE = "ERRORLEVEL-PREAMBLE";

    private static final String ERRORLEVEL_POSTAMBLE = "ERRORLEVEL-POSTAMBLE";
    private String address;
    private int port;
    private String password;
    private AddressPortMapper mapper;
    private int connectionTimeoutMillis;

    private int socketTimeoutMillis;
    private OperatingSystemFamily os;
    private OverthereFile workingDirectory;
    private String username;
    private String protocol;
    private CifsConnectionType connectionType = CifsConnectionType.TELNET;

    public TelnetConnection(ConnectionOptions options, AddressPortMapper mapper, OverthereFile workingDirectory) {
        String unmappedAddress = options.get(ADDRESS);
        int unmappedPort = options.get(PORT, connectionType.getDefaultPort(options));
        InetSocketAddress addressPort = mapper.map(createUnresolved(unmappedAddress, unmappedPort));

        this.os = options.getEnum(OPERATING_SYSTEM, OperatingSystemFamily.class);
        this.connectionTimeoutMillis = options.getInteger(CONNECTION_TIMEOUT_MILLIS, CONNECTION_TIMEOUT_MILLIS_DEFAULT);
        this.socketTimeoutMillis = options.getInteger(SOCKET_TIMEOUT_MILLIS, SOCKET_TIMEOUT_MILLIS_DEFAULT);

        this.address = addressPort.getHostName();
        this.port = addressPort.getPort();
        this.username = options.get(USERNAME);
        this.password = options.get(PASSWORD);
        this.mapper = mapper;
        this.workingDirectory = workingDirectory;
        this.protocol = options.get(PROTOCOL);

        checkIsWindowsHost(os, protocol, connectionType);
        checkNotNewStyleWindowsDomain(username, protocol, connectionType);

    }

    @Override
    public OverthereProcess startProcess(final CmdLine cmd) {
        checkNotNull(cmd, "Cannot execute null command line");
        checkArgument(cmd.getArguments().size() > 0, "Cannot execute empty command line");

        final String obfuscatedCmd = cmd.toCommandLine(os, true);
        logger.info("Starting command [{}] on [{}]", obfuscatedCmd, this);

        try {
            final TelnetClient tc = new TelnetClient();
            tc.setSocketFactory(mapper.socketFactory());
            tc.setConnectTimeout(connectionTimeoutMillis);
            tc.addOptionHandler(new WindowSizeOptionHandler(299, 25, true, false, true, false));
            logger.info("Connecting to telnet://{}@{}", username, address);
            tc.connect(address, port);
            tc.setSoTimeout(socketTimeoutMillis);
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

                        send(stdin, cmd.toCommandLine(os, false));

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
                        throw new RuntimeIOException(format("Cannot start command [%s] on [%s]", obfuscatedCmd, TelnetConnection.this), exc);
                    } finally {
                        closeQuietly(toCallersStdout);
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
                        throw new RuntimeIOException(format("Cannot start command [%s] on [%s]", obfuscatedCmd, TelnetConnection.this), exc);
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
                        logger.info("Disconnected from {}", TelnetConnection.this);

                        closeQuietly(toCallersStdout);
                    } catch (IOException exc) {
                        throw new RuntimeIOException(format("Cannot disconnect from %s", TelnetConnection.this), exc);
                    }
                }

                @Override
                public synchronized int exitValue() {
                    if (tc.isConnected()) {
                        throw new IllegalThreadStateException(format("Process for command [%s] on %s is still running", obfuscatedCmd, TelnetConnection.this));
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

    @Override
    public void connect() {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public void setWorkingDirectory(OverthereFile workingDirectory) {
        this.workingDirectory = workingDirectory;
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

    private static Logger logger = LoggerFactory.getLogger(TelnetConnection.class);
}


