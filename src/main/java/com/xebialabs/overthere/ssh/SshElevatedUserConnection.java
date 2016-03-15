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

import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.CmdLineArgument;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;

import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;

import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static com.xebialabs.overthere.util.OverthereUtils.constructPath;

/**
 * A connection to a Unix host using a user elevation command, i.e. SU, SUDO or ALTERNATIVE_SUDO.
 */
abstract class SshElevatedUserConnection extends SshScpConnection {

    public static final String NOELEVATION_PSEUDO_COMMAND = "nosudo";
    public static final String OVERRIDE_ALLOCATE_PTY = "vt220:80:24:0:0";
    protected String elevatedUsername;
    protected String elevatedPassword;
    protected String elevatedPasswordPromptRegex;
    protected String elevationCommandPrefix;
    protected boolean quoteCommand;
    protected boolean preserveAttributesOnCopyFromTempFile;
    protected boolean preserveAttributesOnCopyToTempFile;
    protected boolean overrideUmask;
    protected String copyFromTempFileCommand;
    protected String copyToTempFileCommand;
    protected String overrideUmaskCommand;
    protected String tempMkdirCommand;
    protected String tempMkdirsCommand;

    protected SshElevatedUserConnection(final String type, final ConnectionOptions options, final AddressPortMapper mapper) {
        super(type, options, mapper);
    }

    protected static void checkElevatedPasswordPromptRegex(final SshElevatedUserConnection connection, final String optionKey, final Logger logger) {
        checkArgument(!connection.elevatedPasswordPromptRegex.endsWith("*"), optionKey + " should not end in a wildcard");
        checkArgument(!connection.elevatedPasswordPromptRegex.endsWith("?"), optionKey + " should not end in a wildcard");

        if (!connection.allocateDefaultPty && connection.allocatePty == null) {
            logger.warn("An {} connection requires a pty, allocating a pty with spec [" + OVERRIDE_ALLOCATE_PTY + "].", connection.protocolAndConnectionType);
            connection.allocatePty = OVERRIDE_ALLOCATE_PTY;
        }
    }

    @Override
    protected SshProcess createProcess(final Session session, final CmdLine commandLine) throws TransportException, ConnectionException {
        if (elevatedPasswordPromptRegex == null) {
            return super.createProcess(session, commandLine);
        } else {
            return new SshProcess(this, os, session, commandLine) {
                @Override
                public InputStream getStdout() {
                    return new SshElevatedPasswordHandlingStream(super.getStdout(), getStdin(), elevatedPassword, elevatedPasswordPromptRegex);
                }
            };
        }
    }

    @Override
    protected CmdLine processCommandLine(final CmdLine cmd) {
        CmdLine processedCmd;
        logger.trace("Checking whether to prefix command line with su/sudo: {}", cmd);
        if (startsWithPseudoCommand(cmd, NOELEVATION_PSEUDO_COMMAND)) {
            logger.trace("Not prefixing command line with su/sudo because the " + NOELEVATION_PSEUDO_COMMAND
                    + " pseudo command was present, but the pseudo command will be stripped");
            processedCmd = super.processCommandLine(stripPrefixedPseudoCommand(cmd));
        } else if(quoteCommand) {
            logger.trace("Quoting command line and prefixing it with su/sudo");
            processedCmd = prefixWithElevationCommand(super.processCommandLine(cmd));
        } else {
            logger.trace("Prefixing command line with su/sudo");
            boolean nocd = startsWithPseudoCommand(cmd, NOCD_PSEUDO_COMMAND);
            if (nocd) {
                processedCmd = stripPrefixedPseudoCommand(cmd);
            } else {
                processedCmd = cmd;
            }
            processedCmd = prefixWithElevationCommand(processedCmd);
            if (nocd) {
                processedCmd = prefixWithPseudoCommand(processedCmd, NOCD_PSEUDO_COMMAND);
            }
            processedCmd = super.processCommandLine(processedCmd);
        }
        logger.trace("Processed command line for su/sudo                  : {}", processedCmd);
        return processedCmd;
    }

    CmdLine prefixWithElevationCommand(final CmdLine commandLine) {
        CmdLine commandLineWithSudo = new CmdLine();
        if (quoteCommand) {
            commandLineWithSudo.addTemplatedFragment(elevationCommandPrefix, elevatedUsername);
            commandLineWithSudo.addNested(commandLine);
        } else {
            boolean shouldAddElevationCommand = true;
            for (CmdLineArgument a : commandLine.getArguments()) {
                if(shouldAddElevationCommand && !a.toString(os, false).equals("cd")) {
                    commandLineWithSudo.addTemplatedFragment(elevationCommandPrefix, elevatedUsername);
                }
                shouldAddElevationCommand = false;

                commandLineWithSudo.add(a);

                if (a.toString(os, false).equals("|") || a.toString(os, false).equals(";")) {
                    shouldAddElevationCommand = true;
                }
            }
        }
        return commandLineWithSudo;
    }

    @Override
    public OverthereFile getFile(String hostPath) throws RuntimeIOException {
        return new SshElevatedUserFile(this, hostPath, false);
    }

    @Override
    public OverthereFile getFile(final OverthereFile parent, final String child) throws RuntimeIOException {
        checkParentFile(parent);
        return new SshElevatedUserFile(this, constructPath(parent, child), ((SshElevatedUserFile) parent).isTempFile());
    }

    @Override
    protected OverthereFile getFileForTempFile(final OverthereFile parent, final String name) {
        checkParentFile(parent);
        return new SshElevatedUserFile(this, constructPath(parent, name), true);
    }

    @Override
    public String toString() {
        return protocolAndConnectionType + "://" + username + ":" + elevatedUsername + "@" + host + ":" + port;
    }

    private Logger logger = LoggerFactory.getLogger(SshElevatedUserConnection.class);

}
