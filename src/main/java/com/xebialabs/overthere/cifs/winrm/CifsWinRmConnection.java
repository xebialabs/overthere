/**
 * Copyright (c) 2008, 2012, XebiaLabs B.V., All rights reserved.
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

import com.google.common.io.Closeables;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.cifs.CifsConnection;
import com.xebialabs.overthere.cifs.WinrmHttpsCertificateTrustStrategy;
import com.xebialabs.overthere.cifs.WinrmHttpsHostnameVerificationStrategy;
import com.xebialabs.overthere.cifs.winrm.connector.ApacheHttpComponentsHttpClientHttpConnector;
import com.xebialabs.overthere.cifs.winrm.exception.WinRMRuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_CONTEXT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_ENABLE_HTTPS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_ENVELOP_SIZE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_KERBEROS_ADD_PORT_TO_SPN;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_KERBEROS_DEBUG;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_KERBEROS_USE_HTTP_SPN;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_LOCALE;
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
import static java.lang.String.format;

/**
 * A connection to a Windows host using CIFS and WinRM.
 *
 * Limitations:
 * <ul>
 *     <li>Cannot start a process yet. Rudimentary support is in, but it is not ready yet.</li>
 * </ul>
 */
public class CifsWinRmConnection extends CifsConnection {

    private ConnectionOptions options;

    private URL targetURL;

    private ApacheHttpComponentsHttpClientHttpConnector httpConnector;

    public static final int STDIN_BUF_SIZE = 4096;

    /**
     * Creates a {@link CifsWinRmConnection}. Don't invoke directly. Use
     * {@link Overthere#getConnection(String, ConnectionOptions)} instead.
     */
    public CifsWinRmConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper, true);
        checkArgument(os == WINDOWS, "Cannot start a " + CIFS_PROTOCOL + ":%s connection to a non-Windows operating system", cifsConnectionType.toString().toLowerCase());
        checkArgument(!username.contains("\\"), "Cannot start a " + CIFS_PROTOCOL + ":%s connection with an old-style Windows domain account [%s], use USER@DOMAIN instead.", cifsConnectionType.toString().toLowerCase(), username);

        this.options = options;
        targetURL = getTargetURL(options);
        httpConnector = createHttpConnector(targetURL, options);
    }

    private URL getTargetURL(ConnectionOptions options) {
        final String scheme = options.getBoolean(WINRM_ENABLE_HTTPS, DEFAULT_WINRM_ENABLE_HTTPS) ? "https" : "http";
        final String context = options.get(WINRM_CONTEXT, DEFAULT_WINRM_CONTEXT);
        try {
            return new URL(scheme, address, port, context);
        } catch (MalformedURLException e) {
            throw new WinRMRuntimeIOException("Cannot build a new URL for " + this, e);
        }
    }

    private ApacheHttpComponentsHttpClientHttpConnector createHttpConnector(URL targetURL, ConnectionOptions options) {
        final ApacheHttpComponentsHttpClientHttpConnector httpConnector = new ApacheHttpComponentsHttpClientHttpConnector(username, password, targetURL, unmappedAddress, unmappedPort);
        httpConnector.setHttpsCertTrustStrategy(options.getEnum(WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY, WinrmHttpsCertificateTrustStrategy.class, DEFAULT_WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY));
        httpConnector.setHttpsHostnameVerifyStrategy(options.getEnum(WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY, WinrmHttpsHostnameVerificationStrategy.class, DEFAULT_WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY));
        httpConnector.setKerberosUseHttpSpn(options.getBoolean(WINRM_KERBEROS_USE_HTTP_SPN, DEFAULT_WINRM_KERBEROS_USE_HTTP_SPN));
        httpConnector.setKerberosAddPortToSpn(options.getBoolean(WINRM_KERBEROS_ADD_PORT_TO_SPN, DEFAULT_WINRM_KERBEROS_ADD_PORT_TO_SPN));
        httpConnector.setKerberosDebug(options.getBoolean(WINRM_KERBEROS_DEBUG, DEFAULT_WINRM_KERBEROS_DEBUG));
        return httpConnector;
    }

    private WinRmClient createWinrmClient(URL targetURL, ApacheHttpComponentsHttpClientHttpConnector httpConnector, ConnectionOptions options) {
        final WinRmClient client = new WinRmClient(httpConnector, targetURL);
        client.setTimeout(options.get(WINRM_TIMEMOUT, DEFAULT_WINRM_TIMEOUT));
        client.setEnvelopSize(options.get(WINRM_ENVELOP_SIZE, DEFAULT_WINRM_ENVELOP_SIZE));
        client.setLocale(options.get(WINRM_LOCALE, DEFAULT_WINRM_LOCALE));
        return client;
    }

    @Override
    public OverthereProcess startProcess(final CmdLine commandLine) {
        final String obfuscatedCommandLine = commandLine.toCommandLine(getHostOperatingSystem(), true);

        String cmd = commandLine.toCommandLine(getHostOperatingSystem(), false);
        if (workingDirectory != null) {
            cmd = "CD /D " + workingDirectory.getPath() + " & " + cmd;
        }

        final WinRmClient winRmClient = createWinrmClient(targetURL, httpConnector, options);
        try {
            final PipedInputStream toCallersStdin = new PipedInputStream();
            final PipedOutputStream callersStdin = new PipedOutputStream(toCallersStdin);
            final PipedInputStream callersStdout = new PipedInputStream();
            final PipedOutputStream toCallersStdout = new PipedOutputStream(callersStdout);
            final PipedInputStream callersStderr = new PipedInputStream();
            final PipedOutputStream toCallersStderr = new PipedOutputStream(callersStderr);

            winRmClient.createShell();
            winRmClient.executeCommand(cmd);

            final Exception processInputReaderTheaException[] = new Exception[1];
            final Thread processInputReaderThead = new Thread(format("Input reader for [%s] on [%s]", obfuscatedCommandLine, CifsWinRmConnection.this)) {
                @Override
                public void run() {
                    try {
                        byte[] buf = new byte[STDIN_BUF_SIZE];
                        for(;;) {
                            int n = toCallersStdin.read(buf);
                            if (n == -1)
                                break;
                            if (n == 0)
                                continue;

                            byte[] bufToSend = new byte[n];
                            System.arraycopy(buf, 0, bufToSend, 0, n);
                            winRmClient.sendInput(bufToSend);
                        }
                    } catch(Exception exc) {
                        processInputReaderTheaException[0] = exc;
                    } finally {
                        Closeables.closeQuietly(callersStdin);
                    }
                }
            };
            processInputReaderThead.setDaemon(true);
            processInputReaderThead.start();
            
            final Exception processOutputReaderThreadException[] = new Exception[1];
            final Thread processOutputReaderThread = new Thread(format("Output reader for [%s] on [%s]", obfuscatedCommandLine, CifsWinRmConnection.this)) {
                @Override
                public void run() {
                    try {
                        for (;;) {
                            if (!winRmClient.receiveOutput(toCallersStdout, toCallersStderr))
                                break;
                        }
                    } catch (Exception exc) {
                        processOutputReaderThreadException[0] = exc;
                    } finally {
                        Closeables.closeQuietly(toCallersStdout);
                        Closeables.closeQuietly(toCallersStderr);
                    }
                }
            };
            processOutputReaderThread.start();

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
                            processOutputReaderThread.join();
                        } finally {
                            winRmClient.deleteShell();
                            processTerminated = true;
                        }
                        if(processOutputReaderThreadException[0] != null) {
                            if(processOutputReaderThreadException[0] instanceof RuntimeException) {
                                throw (RuntimeException) processOutputReaderThreadException[0];
                            } else {
                                throw new RuntimeIOException(format("Cannot execute command [%s] on [%s]", obfuscatedCommandLine, CifsWinRmConnection.this), processOutputReaderThreadException[0]);
                            }
                        }
                        return exitValue();
                    } catch (InterruptedException exc) {
                        throw new RuntimeIOException(format("Cannot execute command [%s] on [%s]", obfuscatedCommandLine, CifsWinRmConnection.this), exc);
                    }
                }

                @Override
                public synchronized void destroy() {
                    if (processTerminated) {
                        return;
                    }

                    winRmClient.signal();
                    winRmClient.deleteShell();
                    processTerminated = true;
                }

                @Override
                public synchronized int exitValue() {
                    if (!processTerminated) {
                        throw new IllegalThreadStateException(format("Process for command [%s] on [%s] is still running", obfuscatedCommandLine,
                            CifsWinRmConnection.this));
                    }

                    return winRmClient.exitValue();
                }
            };

        } catch (IOException exc) {
            throw new RuntimeIOException("Cannot execute command " + commandLine + " on " + this, exc);
        }

    }

}
