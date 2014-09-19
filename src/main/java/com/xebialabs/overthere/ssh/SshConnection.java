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
package com.xebialabs.overthere.ssh;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.CmdLineArgument;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.BaseOverthereConnection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.BIND_ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.BIND_PORT;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.ALLOCATE_DEFAULT_PTY;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.ALLOCATE_DEFAULT_PTY_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.ALLOCATE_PTY;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.INTERACTIVE_KEYBOARD_AUTH_PROMPT_REGEX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.INTERACTIVE_KEYBOARD_AUTH_PROMPT_REGEX_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.OPEN_SHELL_BEFORE_EXECUTE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.OPEN_SHELL_BEFORE_EXECUTE_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PASSPHRASE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PORT_DEFAULT_SSH;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PRIVATE_KEY_FILE;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static com.xebialabs.overthere.util.OverthereUtils.constructPath;
import static java.lang.String.format;
import static java.net.InetSocketAddress.createUnresolved;

/**
 * Base class for host connections using SSH.
 */
abstract class SshConnection extends BaseOverthereConnection {

    public static final String PTY_PATTERN = "([\\w-]+):(\\d+):(\\d+):(\\d+):(\\d+)";

    public static final String NOCD_PSEUDO_COMMAND = "nocd";

    protected SshConnectionType sshConnectionType;

    protected String host;

    protected int port;

    protected String bindAddress;

    protected int bindPort;

    protected String username;

    protected String password;

    protected String interactiveKeyboardAuthPromptRegex;

    protected String privateKeyFile;

    protected String passphrase;

    protected boolean allocateDefaultPty;

    protected boolean openShellBeforeExecute;

    protected String allocatePty;

    protected SSHClient sshClient;

    private static final Pattern ptyPattern = Pattern.compile(PTY_PATTERN);

    private static final Config config = new DefaultConfig();

    @VisibleForTesting
    protected Factory<SSHClient> sshClientFactory = new Factory<SSHClient>() {
        @Override
        public SSHClient create() {
            return new SSHClient(config);
        }
    };

    public SshConnection(final String protocol, final ConnectionOptions options, final AddressPortMapper mapper) {
        super(protocol, options, mapper, true);
        sshConnectionType = options.getEnum(CONNECTION_TYPE, SshConnectionType.class);
        String unmappedAddress = options.get(ADDRESS);
        int unmappedPort = options.getInteger(PORT, PORT_DEFAULT_SSH);
        InetSocketAddress addressPort = mapper.map(createUnresolved(unmappedAddress, unmappedPort));
        host = addressPort.getHostName();
        port = addressPort.getPort();
        bindAddress = options.get(BIND_ADDRESS, "0.0.0.0");
        bindPort = options.getInteger(BIND_PORT, 0);
        username = options.get(USERNAME);
        password = options.getOptional(PASSWORD);
        interactiveKeyboardAuthPromptRegex = options.get(INTERACTIVE_KEYBOARD_AUTH_PROMPT_REGEX, INTERACTIVE_KEYBOARD_AUTH_PROMPT_REGEX_DEFAULT);
        privateKeyFile = options.getOptional(PRIVATE_KEY_FILE);
        passphrase = options.getOptional(PASSPHRASE);
        allocateDefaultPty = options.getBoolean(ALLOCATE_DEFAULT_PTY, ALLOCATE_DEFAULT_PTY_DEFAULT);
        if (allocateDefaultPty) {
            logger.warn("The " + ALLOCATE_DEFAULT_PTY + " connection option has been deprecated in favour of the " + ALLOCATE_PTY + " option. See https://github.com/xebialabs/overthere#ssh_allocatePty");
        }
        allocatePty = options.getOptional(ALLOCATE_PTY);
        openShellBeforeExecute = options.getBoolean(OPEN_SHELL_BEFORE_EXECUTE, OPEN_SHELL_BEFORE_EXECUTE_DEFAULT);
    }

    protected void connect() {
        try {
            SSHClient client = sshClientFactory.create();
            client.setConnectTimeout(connectionTimeoutMillis);
            client.addHostKeyVerifier(new PromiscuousVerifier());

            try {
                client.connect(host, port, InetAddress.getByName(bindAddress), bindPort);
            } catch (IOException e) {
                throw new RuntimeIOException("Cannot connect to " + host + ":" + port, e);
            }

            if (privateKeyFile != null) {
                if (password != null) {
                    logger.warn("The " + PRIVATE_KEY_FILE + " and " + PASSWORD + " connection options have both been set for the connection {}. Ignoring "
                            + PASSWORD
                            + " and using " + PRIVATE_KEY_FILE + ".", this);
                }
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
                checkArgument(matcher.matches(), "Value for allocatePty [%s] does not match pattern \"" + PTY_PATTERN + "\"", allocateDefaultPty);

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
        return "ssh:" + sshConnectionType.toString().toLowerCase() + "://" + username + "@" + host + ":" + port;
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
