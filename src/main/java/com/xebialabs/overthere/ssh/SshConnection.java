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
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PASSPHRASE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PRIVATE_KEY_FILE;

import java.io.IOException;
import java.util.Random;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * Base class for host connections using SSH.
 */
abstract class SshConnection extends OverthereConnection {

    protected String host;

    protected int port;

    protected String username;

    protected String password;

	protected String privateKeyFile;

	protected String passphrase;

	protected SSHClient sshClient;

	public SshConnection(String type, ConnectionOptions options) {
        super(type, options);
        this.host = options.get(ADDRESS);
        this.port = options.get(PORT, 22);
        this.username = options.get(USERNAME);
        this.password = options.get(PASSWORD);
		this.privateKeyFile = options.get(PRIVATE_KEY_FILE);
		this.passphrase = options.get(PASSPHRASE);
    }

	protected void connect() {
        try {
            SSHClient client = new SSHClient();
            client.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            client.addHostKeyVerifier(new LaxKeyVerifier());
            
            try {
                client.connect(host, port);
            } catch (IOException e) {
                throw new RuntimeIOException("Cannot connect to " + host + ":" + port, e);
            }
            
            if (password != null) {
            	client.authPassword(username, password);
            	if (privateKeyFile != null) {
            		logger.warn("Both password and private key have been set for SSH connection {}. Using the password and ignoring the private key.", this);
            	}
            } else if (privateKeyFile != null) {
            	KeyProvider keys;
            	try {
            		if (passphrase == null) {
            			keys = client.loadKeys(privateKeyFile);
            		} else {
            			keys = client.loadKeys(privateKeyFile, passphrase);
            		}
            	} catch (IOException e) {
            		throw new RuntimeIOException("Cannot read key from private key file " + privateKeyFile, e);
            	}
            	client.authPublickey(username, keys);
            }
			sshClient = client;
        } catch (SSHException e) {
            throw new RuntimeIOException("Cannot connect to " + this, e);
        }		
	}

    @Override
    public void doDisconnect() {
    	checkState(sshClient != null, "Already disconnected");
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

    public OverthereFile getFile(String hostPath) throws RuntimeIOException {
        return getFile(hostPath, false);
    }

    protected abstract OverthereFile getFile(String hostPath, boolean isTempFile) throws RuntimeIOException;

    public OverthereFile getFile(OverthereFile parent, String child) throws RuntimeIOException {
        return getFile(parent, child, false);
    }

    protected OverthereFile getFile(OverthereFile parent, String child, boolean isTempFile) throws RuntimeIOException {
        if (!(parent instanceof SshFile)) {
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
    	logger.info("Executing command {} on {}", commandLine, this);
        try {
            return createProcess(getSshClient().startSession(), commandLine);
        } catch (SSHException e) {
            throw new RuntimeIOException("Cannot execute remote command \"" + commandLine.toCommandLine(getHostOperatingSystem(), true) + "\" on " + this, e);
        }

    }

    protected SshProcess createProcess(Session session, CmdLine commandLine) throws TransportException, ConnectionException {
        return new SshProcess(this, session, commandLine);
    }

    @Override
    public String toString() {
        return type + "://" + username + "@" + host + ":" + port;
    }

    private static Logger logger = LoggerFactory.getLogger(SshConnection.class);

}
