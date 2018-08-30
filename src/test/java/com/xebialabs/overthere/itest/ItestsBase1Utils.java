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
package com.xebialabs.overthere.itest;

import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;
import com.xebialabs.overthere.*;
import com.xebialabs.overthere.ssh.SshConnectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.*;
import java.util.Random;

import static com.google.common.io.ByteStreams.toByteArray;
import static com.google.common.io.ByteStreams.write;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL;
import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;
import static com.xebialabs.overthere.smb.SmbConnectionBuilder.SMB_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static java.lang.String.format;

public abstract class ItestsBase1Utils {

    private static final int MAX_RETRY_COUNT = 20;
    protected TemporaryFolder temp;
    protected String protocol;
    protected ConnectionOptions options;
    protected String expectedConnectionClassName;
    protected OverthereConnection connection;
    protected Exception setupException;

    @BeforeMethod
    public void setupHost() throws Exception {
            temp = new TemporaryFolder();
            temp.create();

            protocol = getProtocol();
            options = getOptions();
            expectedConnectionClassName = getExpectedConnectionClassName();
            int retryCount = 0;
            while (connection == null) {
                try {
                    connection = Overthere.getConnection(protocol, options);
                } catch (Exception exc) {
                    ++retryCount;
                    if (retryCount > MAX_RETRY_COUNT) {
                        setupException = exc;
                        throw exc;
                    }
                    System.out.println("Connection Failed, attempting " + retryCount + " try ");
                }
            }
    }

    protected abstract String getProtocol();

    protected abstract ConnectionOptions getOptions();

    protected abstract String getExpectedConnectionClassName();

    @AfterMethod(alwaysRun = true)
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (Exception exc) {
                System.out.println("Exception while disconnecting at end of test case:");
                exc.printStackTrace(System.out);
            } catch (AssertionError e) {
                System.out.println("Ignoring " + e);
            }
        }
        if (temp != null) {
            temp.delete();
        }
    }

    protected static byte[] readFile(final OverthereFile f) {
        try {
            return toByteArray(new InputSupplier<InputStream>() {
                @Override
                public InputStream getInput() throws IOException {
                    return f.getInputStream();
                }
            });
        } catch (IOException exc) {
            throw new RuntimeIOException(format("Cannot read file [%s]", f), exc);
        }

    }

    protected static void writeData(final OverthereFile f, byte[] data) {
        try {
            write(data, new OutputSupplier<OutputStream>() {
                @Override
                public OutputStream getOutput() throws IOException {
                    return f.getOutputStream();
                }
            });
        } catch (IOException exc) {
            throw new RuntimeIOException(format("Cannot write data to file [%s]", f), exc);
        }
    }

    protected static byte[] writeRandomBytes(final File f, final int size) throws IOException {
        byte[] randomBytes = generateRandomBytes(size);
        write(randomBytes, new OutputSupplier<OutputStream>() {
            @Override
            public OutputStream getOutput() throws IOException {
                return new FileOutputStream(f);
            }
        });
        return randomBytes;
    }

    protected static byte[] generateRandomBytes(final int size) {
        byte[] randomBytes = new byte[size];
        new Random().nextBytes(randomBytes);
        return randomBytes;
    }

    protected void checkConnected(String assumptionName) throws IllegalStateException {
        if (setupException != null) {
            throw new IllegalStateException("Cannot check " + assumptionName + " assumption because an exception was thrown while setting up the connection", setupException);
        }

        if (connection == null) {
            throw new IllegalStateException("There was no exception, but still the connection was null while checking the " + assumptionName + " assumption");
        }
    }

    public boolean notLocal() {
        checkConnected("notLocal");
        return !protocol.equals(LOCAL_PROTOCOL);
    }

    public boolean onlyLocal() {
        checkConnected("local");
        return protocol.equals(LOCAL_PROTOCOL);
    }

    public boolean notCifs() {
        checkConnected("notCifs");
        return !protocol.equals(CIFS_PROTOCOL);
    }

    public boolean withPassword() {
        checkConnected("withPassword");
        return options.containsKey("password");
    }

    public boolean onUnix() {
        checkConnected("onUnix");
        return connection.getHostOperatingSystem().equals(UNIX);
    }

    public boolean onWindows() {
        checkConnected("onWindows");
        return connection.getHostOperatingSystem().equals(WINDOWS);
    }

    public boolean onlyCifs() {
        checkConnected("notLocal");
        return protocol.equals(CIFS_PROTOCOL);
    }

    public boolean onlyCifsWinrm() {
        checkConnected("onlyCifsWinrm");
        return protocol.equals(CIFS_PROTOCOL) && options.get(CONNECTION_TYPE).equals(WINRM_INTERNAL);
    }

    public boolean onlyCifsTelnet() {
        checkConnected("onlyCifsTelnet");
        return protocol.equals(CIFS_PROTOCOL) && options.get(CONNECTION_TYPE).equals(TELNET);
    }

    public boolean onlySmbWinrm() {
        checkConnected("onlySmbWinrm");
        return protocol.equals(SMB_PROTOCOL) && options.get(CONNECTION_TYPE).equals(WINRM_INTERNAL);
    }

    public boolean onlySmbTelnet() {
        checkConnected("onlySmbTelnet");
        return protocol.equals(SMB_PROTOCOL) && options.get(CONNECTION_TYPE).equals(TELNET);
    }

    public boolean notSftpCygwin() {
        checkConnected("notSftpCygwin");
        return !onlySftpCygwin();
    }

    public boolean onlySftpCygwin() {
        checkConnected("onlySftpCygwin");
        return SshConnectionType.SFTP_CYGWIN.equals(options.get(CONNECTION_TYPE, null));
    }

    public boolean notSftpWinsshd() {
        checkConnected("notSftpWinsshd");
        return !onlySftpWinsshd();
    }

    public boolean onlySftpWinsshd() {
        checkConnected("onlySftpWinsshd");
        return SshConnectionType.SFTP_WINSSHD.equals(options.get(CONNECTION_TYPE, null));
    }

    public boolean supportsProcess() {
        checkConnected("supportsProcess");
        return connection.canStartProcess();
    }

    public boolean notSupportsProcess() {
        checkConnected("notSupportsProcess");
        return !supportsProcess();
    }

    protected static Logger logger = LoggerFactory.getLogger(OverthereConnectionItestBase.class);

}
