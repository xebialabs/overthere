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
package com.xebialabs.overthere.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.annotations.VisibleForTesting;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.spi.AddressPortMapper;

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.*;

/**
 * A connection to a Unix host using SSH w/ SUDO.
 */
class SshSudoConnection extends SshScpConnection {

    public static final String NOSUDO_PSEUDO_COMMAND = "nosudo";

    protected final String sudoUsername;

    protected final String sudoCommandPrefix;

    protected final boolean sudoQuoteCommand;

    protected final boolean sudoPreserveAttributesOnCopyFromTempFile;
    
    protected final boolean sudoPreserveAttributesOnCopyToTempFile;

    protected final boolean sudoOverrideUmask;

    public SshSudoConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper);
        this.sudoUsername = options.get(SUDO_USERNAME);
        this.sudoCommandPrefix = options.get(SUDO_COMMAND_PREFIX, SUDO_COMMAND_PREFIX_DEFAULT);
        this.sudoQuoteCommand = options.getBoolean(SUDO_QUOTE_COMMAND, SUDO_QUOTE_COMMAND_DEFAULT);
        this.sudoPreserveAttributesOnCopyFromTempFile = options.getBoolean(SUDO_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE, SUDO_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE_DEFAULT);
        this.sudoPreserveAttributesOnCopyToTempFile = options.getBoolean(SUDO_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE, SUDO_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE_DEFAULT);
        this.sudoOverrideUmask = options.get(SUDO_OVERRIDE_UMASK, SUDO_OVERRIDE_UMASK_DEFAULT);
    }

    @Override
    protected CmdLine processCommandLine(final CmdLine commandLine) {
        CmdLine cmd;
        if (startsWithPseudoCommand(commandLine, NOSUDO_PSEUDO_COMMAND)) {
            logger.trace("Not prefixing command line with sudo statement because the " + NOSUDO_PSEUDO_COMMAND
                + " pseudo command was present, but the pseudo command will be stripped");
            logger.trace("Replacing: {}", commandLine);
            cmd = stripPrefixedPseudoCommand(commandLine);
            logger.trace("With     : {}", cmd);
        } else {
            logger.trace("Prefixing command line with sudo statement");
            logger.trace("Replacing: {}", commandLine);
            boolean nocd = startsWithPseudoCommand(commandLine, NOCD_PSEUDO_COMMAND);
            if (nocd) {
                cmd = stripPrefixedPseudoCommand(commandLine);
            } else {
                cmd = commandLine;
            }
            cmd = prefixWithSudoCommand(cmd);
            if (nocd) {
                cmd = prefixWithPseudoCommand(cmd, NOCD_PSEUDO_COMMAND);
            }
            logger.trace("With     : {}", cmd);
        }
        return super.processCommandLine(cmd);
    }

    @VisibleForTesting
    CmdLine prefixWithSudoCommand(final CmdLine commandLine) {
        CmdLine commandLineWithSudo = new CmdLine();
        commandLineWithSudo.addTemplatedFragment(sudoCommandPrefix, sudoUsername);
        if (sudoQuoteCommand) {
            commandLineWithSudo.addNested(commandLine);
        } else {
            for (CmdLineArgument a : commandLine.getArguments()) {
                commandLineWithSudo.add(a);
                if (a.toString(os, false).equals("|") || a.toString(os, false).equals(";")) {
                    commandLineWithSudo.addTemplatedFragment(sudoCommandPrefix, sudoUsername);
                }
            }
        }
        return commandLineWithSudo;
    }

    @Override
    protected OverthereFile getFile(String hostPath, boolean isTempFile) throws RuntimeIOException {
        return new SshSudoFile(this, hostPath, isTempFile);
    }

    @Override
    public String toString() {
        return "ssh:" + sshConnectionType.toString().toLowerCase() + "://" + username + ":" + sudoUsername + "@" + host + ":" + port;
    }

    private Logger logger = LoggerFactory.getLogger(SshSudoFile.class);

}
