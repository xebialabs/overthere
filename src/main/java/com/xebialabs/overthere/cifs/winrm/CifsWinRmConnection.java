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
package com.xebialabs.overthere.cifs.winrm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.cifs.CifsConnection;
import com.xebialabs.overthere.cifs.WinrmHttpsCertificateTrustStrategy;
import com.xebialabs.overthere.cifs.WinrmHttpsHostnameVerificationStrategy;
import com.xebialabs.overthere.spi.AddressPortMapper;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_CONTEXT_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_ENABLE_HTTPS_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_ENVELOP_SIZE_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_KERBEROS_ADD_PORT_TO_SPN_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_KERBEROS_DEBUG_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_KERBEROS_USE_HTTP_SPN_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_LOCALE_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_TIMEOUT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_CONTEXT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_ENABLE_HTTPS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_ENVELOP_SIZE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_KERBEROS_ADD_PORT_TO_SPN;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_KERBEROS_DEBUG;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_KERBEROS_USE_HTTP_SPN;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_LOCALE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_TIMEMOUT;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static java.lang.String.format;

/**
 * A connection to a Windows host using CIFS and WinRM.
 * <p/>
 * Limitations:
 * <ul>
 * <li>Cannot start a process yet. Rudimentary support is in, but it is not ready yet.</li>
 * </ul>
 */
public class CifsWinRmConnection extends CifsConnection {

    private ConnectionOptions options;

    public static final int STDIN_BUF_SIZE = 4096;

    /**
     * Creates a {@link CifsWinRmConnection}. Don't invoke directly. Use
     * {@link Overthere#getConnection(String, ConnectionOptions)} instead.
     */
    public CifsWinRmConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper, true);
        checkArgument(os == WINDOWS, "Cannot create a " + CIFS_PROTOCOL + ":%s connection to a host that is not running Windows", cifsConnectionType.toString().toLowerCase());
        checkArgument(!username.contains("\\"), "Cannot create a " + CIFS_PROTOCOL + ":%s connection with an old-style Windows domain account [%s], use USER@DOMAIN instead.", cifsConnectionType.toString().toLowerCase(), username);

        this.options = options;
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
                            winRmClient.deleteShell();
                            closeQuietly(callersStdin);
                            processTerminated = true;
                        }
                        if (outputReaderThreadException[0] != null) {
                            if (outputReaderThreadException[0] instanceof RuntimeException) {
                                throw (RuntimeException) outputReaderThreadException[0];
                            } else {
                                throw new RuntimeIOException(format("Cannot execute command [%s] on [%s]", obfuscatedCmd, CifsWinRmConnection.this), outputReaderThreadException[0]);
                            }
                        }
                        return exitValue();
                    } catch (InterruptedException exc) {
                        throw new RuntimeIOException(format("Cannot execute command [%s] on [%s]", obfuscatedCmd, CifsWinRmConnection.this), exc);
                    }
                }

                @Override
                public synchronized void destroy() {
                    if (processTerminated) {
                        return;
                    }

                    winRmClient.signal();
                    winRmClient.deleteShell();
                    closeQuietly(callersStdin);
                    processTerminated = true;
                }

                @Override
                public synchronized int exitValue() {
                    if (!processTerminated) {
                        throw new IllegalThreadStateException(format("Process for command [%s] on [%s] is still running", obfuscatedCmd,
                                CifsWinRmConnection.this));
                    }

                    return winRmClient.exitValue();
                }
            };

        } catch (IOException exc) {
            throw new RuntimeIOException("Cannot execute command " + cmd + " on " + this, exc);
        }
    }

    private WinRmClient createWinrmClient() {
        final WinRmClient client = new WinRmClient(username, password, createWinrmURL(), unmappedAddress, unmappedPort);
        client.setWinRmTimeout(options.get(WINRM_TIMEMOUT, DEFAULT_WINRM_TIMEOUT));
        client.setWinRmEnvelopSize(options.get(WINRM_ENVELOP_SIZE, WINRM_ENVELOP_SIZE_DEFAULT));
        client.setWinRmLocale(options.get(WINRM_LOCALE, WINRM_LOCALE_DEFAULT));
        client.setHttpsCertTrustStrategy(options.getEnum(WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY, WinrmHttpsCertificateTrustStrategy.class, WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY_DEFAULT));
        client.setHttpsHostnameVerifyStrategy(options.getEnum(WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY, WinrmHttpsHostnameVerificationStrategy.class, WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY_DEFAULT));
        client.setKerberosUseHttpSpn(options.getBoolean(WINRM_KERBEROS_USE_HTTP_SPN, WINRM_KERBEROS_USE_HTTP_SPN_DEFAULT));
        client.setKerberosAddPortToSpn(options.getBoolean(WINRM_KERBEROS_ADD_PORT_TO_SPN, WINRM_KERBEROS_ADD_PORT_TO_SPN_DEFAULT));
        client.setKerberosDebug(options.getBoolean(WINRM_KERBEROS_DEBUG, WINRM_KERBEROS_DEBUG_DEFAULT));
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

    private static Logger logger = LoggerFactory.getLogger(CifsWinRmConnection.class);

}
