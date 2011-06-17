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

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.Random;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;

/**
 * Base class for host connections using SSH.
 */
abstract class SshOverthereConnection extends OverthereConnection implements OverthereConnectionBuilder {

    protected String host;

    protected int port;

    protected String username;

    protected String password;

    protected SSHClient sshClient;

    public SshOverthereConnection(String type, ConnectionOptions options) {
        super(type, options);
        this.host = options.get(ConnectionOptions.ADDRESS);
        this.port = options.get(ConnectionOptions.PORT, 22);
        this.username = options.get(ConnectionOptions.USERNAME);
        this.password = options.get(ConnectionOptions.PASSWORD);
    }

    @Override
    public SshOverthereConnection connect() throws RuntimeIOException {
        try {
            sshClient = openSession();
        } catch (SSHException e) {
            throw new RuntimeIOException("Cannot connect to " + this, e);
        }
        return this;
    }

    @Override
    public void doDisconnect() {
        checkState(sshClient != null, "Already disconnected");
        super.doDisconnect();
        try {
            sshClient.disconnect();
        } catch (IOException e) {
            throw new RuntimeIOException("Could not disconnect from " + this, e);
        } finally {
            sshClient = null;
        }
    }

    protected SSHClient getSshClient() {
        checkState(sshClient != null, "Not (yet) connected");
        return sshClient;
    }

    protected SSHClient openSession() throws UserAuthException, TransportException {
        SSHClient client = new SSHClient();
        client.addHostKeyVerifier(new LaxKeyVerifier());

        try {
            client.connect(host, port);
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot connect to " + host + ":" + port, e);
        }

        client.authPassword(username, password);

//		final String privateKeyFilename = System.getProperty("ssh.privatekey.filename");
//		if (privateKeyFilename != null) {
//			logger.info(format("found in System properties a private key filename '%s'", privateKeyFilename));
//			jsch.addIdentity(privateKeyFilename, System.getProperty("ssh.privatekey.passphrase"));
//		}
//
//		Session session = jsch.getSession(username, host, port);
//
//		session.setUserInfo(getUserInfo());
//		session.connect(DEFAULT_CONNECTION_TIMEOUT_MS);

        return client;
    }

    public OverthereFile getFile(String hostPath) throws RuntimeIOException {
        return getFile(hostPath, false);
    }

    protected abstract OverthereFile getFile(String hostPath, boolean isTempFile) throws RuntimeIOException;

    public OverthereFile getFile(OverthereFile parent, String child) throws RuntimeIOException {
        return getFile(parent, child, false);
    }

    protected OverthereFile getFile(OverthereFile parent, String child, boolean isTempFile) throws RuntimeIOException {
        if (!(parent instanceof SshOverthereFile)) {
            throw new IllegalStateException("parent is not a file on an SSH host");
        }
        if (parent.getConnection() != this) {
            throw new IllegalStateException("parent is not a file in this connection");
        }
        return getFile(parent.getPath() + getHostOperatingSystem().getFileSeparator() + child, isTempFile);
    }

    // FIXME: Move to OverthereHostConnectionUtils
    public OverthereFile getTempFile(String prefix, String suffix) throws RuntimeIOException {
        if (prefix == null)
            throw new NullPointerException("prefix is null");

        if (suffix == null) {
            suffix = ".tmp";
        }

        Random r = new Random();
        String infix = "";
        for (int i = 0; i < MAX_TEMP_RETRIES; i++) {
            OverthereFile f = getFile(getTempDirectory().getPath() + getHostOperatingSystem().getFileSeparator() + prefix + infix + suffix, true);
            if (!f.exists()) {
                if (logger.isDebugEnabled())
                    logger.debug("Created temporary file " + f);

                return f;
            }
            infix = "-" + Long.toString(Math.abs(r.nextLong()));
        }
        throw new RuntimeIOException("Cannot generate a unique temporary file name on " + this);
    }

    public OverthereProcess startProcess(final CmdLine commandLine) {
        try {
            return createProcess(getSshClient().startSession(), commandLine);
        } catch (SSHException e) {
            throw new RuntimeIOException("Cannot execute remote command \"" + commandLine.toCommandLine(getHostOperatingSystem(), true) + "\" on " + this, e);
        }

    }

    protected SshProcess createProcess(Session session, CmdLine commandLine) throws TransportException, ConnectionException {
        return new SshProcess(this, session, commandLine);
    }

    static int waitForExitStatus(Session.Subsystem channel, String command) {
        while (true) {
            if (!channel.isOpen()) {
                return channel.getExitStatus();
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException exc) {
                Thread.currentThread().interrupt();
                throw new RuntimeIOException("Remote command \"" + command + "\" was interrupted", exc);
            }
        }
    }

    @Override
    public String toString() {
        return type + "://" + username + "@" + host + ":" + port;
    }

    private static Logger logger = LoggerFactory.getLogger(SshOverthereConnection.class);

}
