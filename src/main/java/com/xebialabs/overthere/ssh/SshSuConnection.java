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

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_COMMAND_PREFIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_COMMAND_PREFIX_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_COPY_FROM_TEMP_FILE_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_COPY_FROM_TEMP_FILE_COMMAND_DEFAULT_NO_PRESERVE_ATTRIBUTES;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_COPY_FROM_TEMP_FILE_COMMAND_DEFAULT_PRESERVE_ATTRIBUTES;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_COPY_TO_TEMP_FILE_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_COPY_TO_TEMP_FILE_COMMAND_DEFAULT_NO_PRESERVE_ATTRIBUTES;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_COPY_TO_TEMP_FILE_COMMAND_DEFAULT_PRESERVE_ATTRIBUTES;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_OVERRIDE_UMASK;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_OVERRIDE_UMASK_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_OVERRIDE_UMASK_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_OVERRIDE_UMASK_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_PASSWORD;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_PASSWORD_PROMPT_REGEX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_PASSWORD_PROMPT_REGEX_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_QUOTE_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_QUOTE_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_TEMP_MKDIRS_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_TEMP_MKDIRS_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_TEMP_MKDIR_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_TEMP_MKDIR_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SU_USERNAME;

/**
 * A connection to a Unix host using SSH w/ SU.
 */
class SshSuConnection extends SshElevatedUserConnection {

    public SshSuConnection(final String type, final ConnectionOptions options, final AddressPortMapper mapper) {
        super(type, options, mapper);

        elevatedUsername = options.get(SU_USERNAME);
        elevatedPassword = options.get(SU_PASSWORD);
        elevatedPasswordPromptRegex = options.get(SU_PASSWORD_PROMPT_REGEX, SU_PASSWORD_PROMPT_REGEX_DEFAULT);

        elevationCommandPrefix = options.get(SU_COMMAND_PREFIX, SU_COMMAND_PREFIX_DEFAULT);
        quoteCommand = options.getBoolean(SU_QUOTE_COMMAND, SU_QUOTE_COMMAND_DEFAULT);
        preserveAttributesOnCopyFromTempFile = options.getBoolean(SU_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE, SU_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE_DEFAULT);
        preserveAttributesOnCopyToTempFile = options.getBoolean(SU_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE, SU_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE_DEFAULT);
        overrideUmask = options.getBoolean(SU_OVERRIDE_UMASK, SU_OVERRIDE_UMASK_DEFAULT);

        copyFromTempFileCommand = options.get(SU_COPY_FROM_TEMP_FILE_COMMAND, preserveAttributesOnCopyFromTempFile ? SU_COPY_FROM_TEMP_FILE_COMMAND_DEFAULT_PRESERVE_ATTRIBUTES : SU_COPY_FROM_TEMP_FILE_COMMAND_DEFAULT_NO_PRESERVE_ATTRIBUTES);
        copyToTempFileCommand = options.get(SU_COPY_TO_TEMP_FILE_COMMAND, preserveAttributesOnCopyToTempFile ? SU_COPY_TO_TEMP_FILE_COMMAND_DEFAULT_PRESERVE_ATTRIBUTES : SU_COPY_TO_TEMP_FILE_COMMAND_DEFAULT_NO_PRESERVE_ATTRIBUTES);
        overrideUmaskCommand = options.get(SU_OVERRIDE_UMASK_COMMAND, SU_OVERRIDE_UMASK_COMMAND_DEFAULT);
        tempMkdirCommand = options.get(SU_TEMP_MKDIR_COMMAND, SU_TEMP_MKDIR_COMMAND_DEFAULT);
        tempMkdirsCommand = options.get(SU_TEMP_MKDIRS_COMMAND, SU_TEMP_MKDIRS_COMMAND_DEFAULT);

        checkElevatedPasswordPromptRegex(this, SU_PASSWORD_PROMPT_REGEX, logger);
    }

    private static Logger logger = LoggerFactory.getLogger(SshSuConnection.class);

}
