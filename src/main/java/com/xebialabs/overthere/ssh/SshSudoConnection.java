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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.spi.AddressPortMapper;

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_COMMAND_PREFIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_COMMAND_PREFIX_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_COPY_FROM_TEMP_FILE_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_COPY_FROM_TEMP_FILE_COMMAND_DEFAULT_NO_PRESERVE_ATTRIBUTES;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_COPY_FROM_TEMP_FILE_COMMAND_DEFAULT_PRESERVE_ATTRIBUTES;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_COPY_TO_TEMP_FILE_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_COPY_TO_TEMP_FILE_COMMAND_DEFAULT_NO_PRESERVE_ATTRIBUTES;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_COPY_TO_TEMP_FILE_COMMAND_DEFAULT_PRESERVE_ATTRIBUTES;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_OVERRIDE_UMASK;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_OVERRIDE_UMASK_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_OVERRIDE_UMASK_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_OVERRIDE_UMASK_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_QUOTE_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_QUOTE_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_TEMP_MKDIRS_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_TEMP_MKDIRS_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_TEMP_MKDIR_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_TEMP_MKDIR_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_USERNAME;

/**
 * A connection to a Unix host using SSH w/ SUDO.
 */
class SshSudoConnection extends SshElevatedUserConnection {

    public SshSudoConnection(final String type, final ConnectionOptions options, final AddressPortMapper mapper) {
        super(type, options, mapper);

        elevatedUsername = options.get(SUDO_USERNAME);
        elevatedPassword = null;
        elevatedPasswordPromptRegex = null;

        elevationCommandPrefix = options.get(SUDO_COMMAND_PREFIX, SUDO_COMMAND_PREFIX_DEFAULT);
        quoteCommand = options.getBoolean(SUDO_QUOTE_COMMAND, SUDO_QUOTE_COMMAND_DEFAULT);
        preserveAttributesOnCopyFromTempFile = options.getBoolean(SUDO_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE, SUDO_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE_DEFAULT);
        preserveAttributesOnCopyToTempFile = options.getBoolean(SUDO_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE, SUDO_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE_DEFAULT);
        overrideUmask = options.getBoolean(SUDO_OVERRIDE_UMASK, SUDO_OVERRIDE_UMASK_DEFAULT);

        copyFromTempFileCommand = options.get(SUDO_COPY_FROM_TEMP_FILE_COMMAND, preserveAttributesOnCopyFromTempFile ? SUDO_COPY_FROM_TEMP_FILE_COMMAND_DEFAULT_PRESERVE_ATTRIBUTES : SUDO_COPY_FROM_TEMP_FILE_COMMAND_DEFAULT_NO_PRESERVE_ATTRIBUTES);
        copyToTempFileCommand = options.get(SUDO_COPY_TO_TEMP_FILE_COMMAND, preserveAttributesOnCopyToTempFile ? SUDO_COPY_TO_TEMP_FILE_COMMAND_DEFAULT_PRESERVE_ATTRIBUTES : SUDO_COPY_TO_TEMP_FILE_COMMAND_DEFAULT_NO_PRESERVE_ATTRIBUTES);
        overrideUmaskCommand = options.get(SUDO_OVERRIDE_UMASK_COMMAND, SUDO_OVERRIDE_UMASK_COMMAND_DEFAULT);
        tempMkdirCommand = options.get(SUDO_TEMP_MKDIR_COMMAND, SUDO_TEMP_MKDIR_COMMAND_DEFAULT);
        tempMkdirsCommand = options.get(SUDO_TEMP_MKDIRS_COMMAND, SUDO_TEMP_MKDIRS_COMMAND_DEFAULT);
    }

    private static Logger logger = LoggerFactory.getLogger(SshSudoConnection.class);

}
