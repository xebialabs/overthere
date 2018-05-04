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

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.BaseOverthereConnection;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.Factory;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.PTYMode;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.FileKeyProvider;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.keyprovider.PKCS5KeyFile;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.*;
import static com.xebialabs.overthere.util.OverthereUtils.*;
import static java.lang.String.format;
import static java.net.InetSocketAddress.createUnresolved;

/**
 * Base class for host connections using SSH.
 */
abstract class SshConnection extends BaseOverthereConnection {

    public static final String PTY_PATTERN = "([\\w-]+):(\\d+):(\\d+):(\\d+):(\\d+)";

    public static final String NOCD_PSEUDO_COMMAND = "nocd";

    protected String protocolAndConnectionType;

    protected String host;

    protected int port;

    protected String localAddress;

    protected int localPort;

    protected String username;

    protected String password;

    protected String interactiveKeyboardAuthPromptRegex;

    protected String privateKey;

    protected String privateKeyFile;

    protected String passphrase;

    protected boolean allocateDefaultPty;

    protected boolean openShellBeforeExecute;

    protected String allocatePty;

    protected int heartbeatInterval;

    protected SSHClient sshClient;

    private static final Pattern ptyPattern = Pattern.compile(PTY_PATTERN);

    private static final Config config = new DefaultConfig();
    {
        // PKCS5 is missing from 0.19.0 SSHJ config.
        List<Factory.Named<FileKeyProvider>> current = config.getFileKeyProviderFactories();
        current = new ArrayList<>(current);
        current.add(new PKCS5KeyFile.Factory());
        config.setFileKeyProviderFactories(current);
    }

    protected Factory<SSHClient> sshClientFactory = new Factory<SSHClient>() {
        @Override
        public SSHClient create() {
            return new SSHClient(config);
        }
    };

    public SshConnection(final String protocol, final ConnectionOptions options, final AddressPortMapper mapper) {
        super(protocol, options, mapper, true);
        SshConnectionType connectionType = options.getOptionalEnum(CONNECTION_TYPE, SshConnectionType.class);
        if(connectionType != null) {
            protocolAndConnectionType = protocol + ":" + connectionType.toString().toLowerCase();
        } else {
            protocolAndConnectionType = protocol;
        }
        String unmappedAddress = options.get(ADDRESS);
        int unmappedPort = options.getInteger(PORT, PORT_DEFAULT_SSH);
        InetSocketAddress addressPort = mapper.map(createUnresolved(unmappedAddress, unmappedPort));
        host = addressPort.getHostName();
        port = addressPort.getPort();
        localAddress = options.getOptional(LOCAL_ADDRESS);
        localPort = options.getInteger(LOCAL_PORT, 0);
        username = options.get(USERNAME);
        password = options.getOptional(PASSWORD);
        interactiveKeyboardAuthPromptRegex = options.get(INTERACTIVE_KEYBOARD_AUTH_PROMPT_REGEX, INTERACTIVE_KEYBOARD_AUTH_PROMPT_REGEX_DEFAULT);
        privateKey = options.getOptional(PRIVATE_KEY);
        privateKeyFile = options.getOptional(PRIVATE_KEY_FILE);
        passphrase = options.getOptional(PASSPHRASE);
        allocateDefaultPty = options.getBoolean(ALLOCATE_DEFAULT_PTY, ALLOCATE_DEFAULT_PTY_DEFAULT);
        heartbeatInterval = options.getInteger(HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL_DEFAULT);
        if (allocateDefaultPty) {
            logger.warn("The " + ALLOCATE_DEFAULT_PTY + " connection option has been deprecated in favour of the " + ALLOCATE_PTY + " option. See https://github.com/xebialabs/overthere#ssh_allocatePty");
        }
        allocatePty = options.getOptional(ALLOCATE_PTY);
        openShellBeforeExecute = options.getBoolean(OPEN_SHELL_BEFORE_EXECUTE, OPEN_SHELL_BEFORE_EXECUTE_DEFAULT);
    }

    protected void connect() {
        try {
            config.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);
            SSHClient client = sshClientFactory.create();
            client.setSocketFactory(mapper.socketFactory());
            client.setConnectTimeout(connectionTimeoutMillis);
            client.addHostKeyVerifier(new PromiscuousVerifier());
            client.setTimeout(socketTimeoutMillis);
            client.getConnection().getKeepAlive().setKeepAliveInterval(heartbeatInterval);

            try {
                if (localAddress == null) {
                    client.connect(host, port);
                } else {
                    client.connect(host, port, InetAddress.getByName(localAddress), localPort);
                }
            } catch (IOException e) {
                throw new RuntimeIOException("Cannot connect to " + host + ":" + port, e);
            }

            if (!onlyOneNotNull(privateKey, privateKeyFile, password)) {
                logger.warn("You should only set one connection options between: {}, {}, {}. They are evaluated in this order, and latter would have no effect on the connection.", PRIVATE_KEY, PRIVATE_KEY_FILE, PASSWORD);
            }

            KeyProvider keys;
            if (privateKey != null) {
                try {
                    if (passphrase == null) {
                        keys = client.loadKeys(privateKey, null, null);
                    } else {
                        keys = client.loadKeys(privateKey, null, getPassphraseFinder());
                    }
                } catch (IOException e) {
                    throw new RuntimeIOException("The supplied key is not in a recognized format", e);
                }
                client.authPublickey(username, keys);
            } else if (privateKeyFile != null) {
                try {
                    if (passphrase == null) {
                        keys = client.loadKeys(privateKeyFile);
                    } else {
                        keys = client.loadKeys(privateKeyFile, getPassphraseFinder());
                    }
                } catch (IOException e) {
                    throw new RuntimeIOException("Cannot read key from private key file " + privateKeyFile, e);
                }
                client.authPublickey(username, keys);
            } else if (password != null) {
                PasswordFinder passwordFinder = getPasswordFinder();
                client.auth(username, new AuthPassword(passwordFinder),
                        new AuthKeyboardInteractive(new RegularExpressionPasswordResponseProvider(passwordFinder, interactiveKeyboardAuthPromptRegex)));
            }

            sshClient = client;
            connected();
        } catch (SSHException e) {
            throw new RuntimeIOException("Cannot connect to " + this, e);
        }
    }

    private PasswordFinder getPasswordFinder() {
        return new PasswordFinder() {

            @Override
            public char[] reqPassword(Resource<?> resource) {
                return password.toCharArray();
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }
        };
    }

    private PasswordFinder getPassphraseFinder() {
        return new PasswordFinder() {

            @Override
            public char[] reqPassword(Resource<?> resource) {
                return passphrase.toCharArray();
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }
        };
    }

    private boolean onlyOneNotNull(Object... objs) {
        int guard = 0;
        for (Object obj: objs) {
            guard += obj != null ? 1 : 0;
        }
        return guard == 1;
    }

    @Override
    public void doClose() {
        if (sshClient == null) return;
        try {
            sshClient.disconnect();
        } catch (Exception e) {
            // Even though we get an exception, we expect the connection to have been closed, so we are ignoring
            logger.error("Unexpected exception received while disconnecting from " + this, e);
        } finally {
            sshClient = null;
        }
    }

    protected SSHClient getSshClient() {
        checkState(sshClient != null, "Not (yet) connected");
        return sshClient;
    }

    @Override
    public OverthereFile getFile(OverthereFile parent, String child) throws RuntimeIOException {
        checkParentFile(parent);
        return getFile(constructPath(parent, child));
    }

    @Override
    protected OverthereFile getFileForTempFile(OverthereFile parent, String name) {
        checkParentFile(parent);
        return getFile(parent, name);
    }

    protected void checkParentFile(final OverthereFile parent) {
        if (!(parent instanceof SshFile)) {
            throw new IllegalStateException("parent is not a file on an SSH host");
        }
        if (parent.getConnection() != this) {
            throw new IllegalStateException("parent is not a file in this connection");
        }
    }

    @Override
    public OverthereProcess startProcess(final CmdLine origCmd) {
        checkNotNull(origCmd, "Cannot execute null command line");
        checkArgument(origCmd.getArguments().size() > 0, "Cannot execute empty command line");

        final CmdLine cmd = processCommandLine(origCmd);

        final String obfuscatedCmd = origCmd.toCommandLine(os, true);
        logger.info("Starting command [{}] on [{}]", obfuscatedCmd, this);

        try {
            if (openShellBeforeExecute) {
                Session session = null;
                try {
                    logger.debug("Creating a temporary shell to allow for deferred home dir creation.");
                    session = getSshClient().startSession();
                    Session.Shell shell = session.startShell();
                    shell.close();
                } finally {
                    closeQuietly(session);
                }
            }

            Session session = getSshClient().startSession();
            if (allocatePty != null && !allocatePty.isEmpty()) {
                if (allocateDefaultPty) {
                    logger.warn("The " + ALLOCATE_PTY + " and " + ALLOCATE_DEFAULT_PTY
                            + " connection options have both been set for the connection {}. Ignoring "
                            + ALLOCATE_DEFAULT_PTY + " and using " + ALLOCATE_PTY + ".", this);
                }
                Matcher matcher = ptyPattern.matcher(allocatePty);
                checkArgument(matcher.matches(), "Value for allocatePty [%s] does not match pattern \"" + PTY_PATTERN + "\"", allocatePty);

                String term = matcher.group(1);
                int cols = Integer.valueOf(matcher.group(2));
                int rows = Integer.valueOf(matcher.group(3));
                int width = Integer.valueOf(matcher.group(4));
                int height = Integer.valueOf(matcher.group(5));
                logger.debug("Allocating PTY {}:{}:{}:{}:{}", new Object[]{term, cols, rows, width, height});
                session.allocatePTY(term, cols, rows, width, height, Collections.<PTYMode, Integer>emptyMap());
            } else if (allocateDefaultPty) {
                logger.debug("Allocating default PTY");
                session.allocateDefaultPTY();
            }
            return createProcess(session, cmd);
        } catch (SSHException e) {
            throw new RuntimeIOException(format("Cannot start command [%s] on [%s]", obfuscatedCmd, this), e);
        }

    }

    protected CmdLine processCommandLine(final CmdLine cmd) {
        CmdLine processedCmd;
        logger.trace("Checking whether to prefix command line with cd: {}", cmd);
        if (startsWithPseudoCommand(cmd, NOCD_PSEUDO_COMMAND)) {
            logger.trace("Not prefixing command line with cd statement because the " + NOCD_PSEUDO_COMMAND
                    + " pseudo command was present, but the pseudo command will be stripped");
            processedCmd = stripPrefixedPseudoCommand(cmd);
        } else if (getWorkingDirectory() != null) {
            logger.trace("Prefixing command line with cd statement because the current working directory was set");
            logger.trace("Replacing: {}", cmd);
            processedCmd = new CmdLine();
            processedCmd.addArgument("cd");
            processedCmd.addArgument(workingDirectory.getPath());
            processedCmd.addRaw(os.getCommandSeparator());
            for (CmdLineArgument a : cmd.getArguments()) {
                processedCmd.add(a);
            }
        } else {
            logger.trace("Not prefixing command line with cd statement because the current working directory was not set");
            processedCmd = cmd;
        }
        logger.trace("Processed command line for cd                  : {}", processedCmd);
        return processedCmd;

    }

    protected boolean startsWithPseudoCommand(final CmdLine commandLine, final String pseudoCommand) {
        return commandLine.getArguments().size() >= 2 && commandLine.getArguments().get(0).toString(os, false).equals(pseudoCommand);
    }

    protected SshProcess createProcess(Session session, CmdLine commandLine) throws TransportException, ConnectionException {
        return new SshProcess(this, os, session, commandLine);
    }

    @Override
    public String toString() {
        return protocolAndConnectionType + "://" + username + "@" + host + ":" + port;
    }

    protected static CmdLine stripPrefixedPseudoCommand(final CmdLine commandLine) {
        return new CmdLine().add(commandLine.getArguments().subList(1, commandLine.getArguments().size()));
    }

    protected static CmdLine prefixWithPseudoCommand(final CmdLine commandLine, final String pseudoCommand) {
        CmdLine nosudoCommandLine = new CmdLine();
        nosudoCommandLine.addArgument(pseudoCommand);
        nosudoCommandLine.add(commandLine.getArguments());
        return nosudoCommandLine;
    }

    private static Logger logger = LoggerFactory.getLogger(SshConnection.class);

}
