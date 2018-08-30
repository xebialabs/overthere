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
package com.xebialabs.overthere.winrm;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import com.xebialabs.overthere.cifs.WinrmHttpsCertificateTrustStrategy;
import com.xebialabs.overthere.cifs.WinrmHttpsHostnameVerificationStrategy;
import com.xebialabs.overthere.spi.ProcessConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.*;
import static com.xebialabs.overthere.cifs.ConnectionValidator.checkIsWindowsHost;
import static com.xebialabs.overthere.cifs.ConnectionValidator.checkNotOldStyleWindowsDomain;
import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static com.xebialabs.overthere.util.OverthereUtils.checkNotNull;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static java.lang.String.format;
import static java.net.InetSocketAddress.createUnresolved;

/**
 * A connection to a Windows host using a Java implementation of WinRM.
 */
public class WinRmConnection implements ProcessConnection {

    private int connectionTimeoutMillis;
    private int socketTimeoutMillis;
    private OperatingSystemFamily os;
    private OverthereFile workingDirectory;
    private AddressPortMapper mapper;
    private String address;
    private int port;
    private String password;
    private String username;
    private String protocol;
    private CifsConnectionType connectionType = CifsConnectionType.WINRM_INTERNAL;
    private ConnectionOptions options;
    private final String unmappedAddress;
    private final int unmappedPort;
    public static final int STDIN_BUF_SIZE = 4096;

    public WinRmConnection(ConnectionOptions options, AddressPortMapper mapper, OverthereFile workingDirectory) {
        this.workingDirectory = workingDirectory;
        this.options = options;
        this.mapper = mapper;

        this.os = options.getEnum(OPERATING_SYSTEM, OperatingSystemFamily.class);
        this.connectionTimeoutMillis = options.getInteger(CONNECTION_TIMEOUT_MILLIS, CONNECTION_TIMEOUT_MILLIS_DEFAULT);
        this.socketTimeoutMillis = options.getInteger(SOCKET_TIMEOUT_MILLIS, SOCKET_TIMEOUT_MILLIS_DEFAULT);

        unmappedAddress = options.get(ADDRESS);
        unmappedPort = options.get(PORT, connectionType.getDefaultPort(options));
        InetSocketAddress addressPort = mapper.map(createUnresolved(unmappedAddress, unmappedPort));
        this.address = addressPort.getHostName();
        this.port = addressPort.getPort();
        this.username = options.get(USERNAME);
        this.password = options.get(PASSWORD);
        this.protocol = options.get(PROTOCOL);

        checkIsWindowsHost(os, protocol, connectionType);
        checkNotOldStyleWindowsDomain(username, protocol, connectionType);
    }

    @Override
    public OverthereProcess startProcess(final CmdLine cmd) {
        checkNotNull(cmd, "Cannot execute null command line");
        checkArgument(cmd.getArguments().size() > 0, "Cannot execute empty command line");

        final String obfuscatedCmd = cmd.toCommandLine(os, true);
        logger.info("Starting command [{}] on [{}]", obfuscatedCmd, this);

        String cmdString = cmd.toCommandLine(os, false);
        if (workingDirectory != null) {
            cmdString = "CD /D " + workingDirectory.getPath() + " & " + cmdString;
        }

        final WinRmClient winRmClient = createWinrmClient();
        try {
            final PipedInputStream fromCallersStdin = new PipedInputStream();
            final PipedOutputStream callersStdin = new PipedOutputStream(fromCallersStdin);
            final PipedInputStream callersStdout = new PipedInputStream();
            final PipedOutputStream toCallersStdout = new PipedOutputStream(callersStdout);
            final PipedInputStream callersStderr = new PipedInputStream();
            final PipedOutputStream toCallersStderr = new PipedOutputStream(callersStderr);

            winRmClient.createShell();
            final String commandId = winRmClient.executeCommand(cmdString);

            final Exception inputReaderTheaException[] = new Exception[1];
            final Thread inputReaderThead = new Thread(format("WinRM input reader for command [%s]", commandId)) {
                @Override
                public void run() {
                    try {
                        byte[] buf = new byte[STDIN_BUF_SIZE];
                        for (; ; ) {
                            int n = fromCallersStdin.read(buf);
                            if (n == -1)
                                break;
                            if (n == 0)
                                continue;

                            byte[] bufToSend = new byte[n];
                            System.arraycopy(buf, 0, bufToSend, 0, n);
                            winRmClient.sendInput(bufToSend);
                        }
                    } catch (Exception exc) {
                        inputReaderTheaException[0] = exc;
                    } finally {
                        closeQuietly(fromCallersStdin);
                    }
                }
            };
            inputReaderThead.setDaemon(true);
            inputReaderThead.start();

            final Exception outputReaderThreadException[] = new Exception[1];
            final Thread outputReaderThread = new Thread(format("WinRM output reader for command [%s]", commandId)) {
                @Override
                public void run() {
                    try {
                        for (; ; ) {
                            if (!winRmClient.receiveOutput(toCallersStdout, toCallersStderr))
                                break;
                        }
                    } catch (Exception exc) {
                        outputReaderThreadException[0] = exc;
                    } finally {
                        closeQuietly(toCallersStdout);
                        closeQuietly(toCallersStderr);
                    }
                }
            };
            outputReaderThread.setDaemon(true);
            outputReaderThread.start();

            return new OverthereProcess() {
                boolean processTerminated = false;

                @Override
                public synchronized OutputStream getStdin() {
                    return callersStdin;
                }

                @Override
                public synchronized InputStream getStdout() {
                    return callersStdout;
                }

                @Override
                public synchronized InputStream getStderr() {
                    return callersStderr;
                }

                @Override
                public synchronized int waitFor() {
                    if (processTerminated) {
                        return exitValue();
                    }

                    try {
                        try {
                            outputReaderThread.join();
                        } finally {
                            closeQuietly(callersStdin);
                            processTerminated = true;
                            try {
                                winRmClient.deleteShell();
                            }catch (Throwable t){
                                logger.warn("Failure while deleting winrm shell", t);
                            }
                        }
                        if (outputReaderThreadException[0] != null) {
                            if (outputReaderThreadException[0] instanceof RuntimeException) {
                                throw (RuntimeException) outputReaderThreadException[0];
                            } else {
                                throw new RuntimeIOException(format("Cannot execute command [%s] on [%s]", obfuscatedCmd, WinRmConnection.this), outputReaderThreadException[0]);
                            }
                        }
                        return exitValue();
                    } catch (InterruptedException exc) {
                        throw new RuntimeIOException(format("Cannot execute command [%s] on [%s]", obfuscatedCmd, WinRmConnection.this), exc);
                    }
                }

                @Override
                public synchronized void destroy() {
                    if (processTerminated) {
                        return;
                    }

                    closeQuietly(callersStdin);
                    processTerminated = true;
                    winRmClient.signal();
                    winRmClient.deleteShell();
                }

                @Override
                public synchronized int exitValue() {
                    if (!processTerminated) {
                        throw new IllegalThreadStateException(format("Process for command [%s] on [%s] is still running", obfuscatedCmd,
                                WinRmConnection.this));
                    }

                    return winRmClient.exitValue();
                }
            };

        } catch (IOException exc) {
            throw new RuntimeIOException("Cannot execute command " + cmd + " on " + this, exc);
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

    private WinRmClient createWinrmClient() {
        final WinRmClient client = new WinRmClient(username, password, createWinrmURL(), unmappedAddress, unmappedPort, mapper.socketFactory());
        client.setWinRmTimeout(options.get(WINRM_TIMEMOUT, DEFAULT_WINRM_TIMEOUT));
        client.setWinRmEnvelopSize(options.get(WINRM_ENVELOP_SIZE, WINRM_ENVELOP_SIZE_DEFAULT));
        client.setWinRmLocale(options.get(WINRM_LOCALE, WINRM_LOCALE_DEFAULT));
        client.setHttpsCertTrustStrategy(options.getEnum(WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY, WinrmHttpsCertificateTrustStrategy.class, WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY_DEFAULT));
        client.setHttpsHostnameVerifyStrategy(options.getEnum(WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY, WinrmHttpsHostnameVerificationStrategy.class, WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY_DEFAULT));
        client.setKerberosUseHttpSpn(options.getBoolean(WINRM_KERBEROS_USE_HTTP_SPN, WINRM_KERBEROS_USE_HTTP_SPN_DEFAULT));
        client.setKerberosAddPortToSpn(options.getBoolean(WINRM_KERBEROS_ADD_PORT_TO_SPN, WINRM_KERBEROS_ADD_PORT_TO_SPN_DEFAULT));
        client.setKerberosDebug(options.getBoolean(WINRM_KERBEROS_DEBUG, WINRM_KERBEROS_DEBUG_DEFAULT));
        client.setKerberosTicketCache(options.getBoolean(WINRM_KERBEROS_TICKET_CACHE, WINRM_KERBEROS_TICKET_CACHE_DEFAULT));
        client.setConnectionTimeout(connectionTimeoutMillis);
        client.setSoTimeout(socketTimeoutMillis);
        client.setUseCanonicalHostname(options.getBoolean(WINRM_USE_CANONICAL_HOSTNAME, WINRM_USE_CANONICAL_HOSTNAME_DEFAULT));
        return client;
    }

    private URL createWinrmURL() {
        final String scheme = options.getBoolean(WINRM_ENABLE_HTTPS, WINRM_ENABLE_HTTPS_DEFAULT) ? "https" : "http";
        final String context = options.get(WINRM_CONTEXT, WINRM_CONTEXT_DEFAULT);
        try {
            return new URL(scheme, address, port, context);
        } catch (MalformedURLException e) {
            throw new WinRmRuntimeIOException("Cannot build a new URL for " + this, e);
        }
    }

    private static Logger logger = LoggerFactory.getLogger(WinRmConnection.class);
}
