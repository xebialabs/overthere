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

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

import static com.xebialabs.overthere.ConnectionOptions.registerFilteredKey;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;

/**
 * Builds SSH connections.
 */
@Protocol(name = SSH_PROTOCOL)
public class SshConnectionBuilder implements OverthereConnectionBuilder {

    /**
     * Name of the protocol handled by this connection builder, i.e. "ssh".
     */
    public static final String SSH_PROTOCOL = "ssh";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_connectionType">the online documentation</a>
     */
    public static final int PORT_DEFAULT_SSH = 22;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_allocateDefaultPty">the online documentation</a>
     */
    public static final String ALLOCATE_DEFAULT_PTY = "allocateDefaultPty";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_allocateDefaultPty">the online documentation</a>
     */
    public static final boolean ALLOCATE_DEFAULT_PTY_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_allocatePty">the online documentation</a>
     */
    public static final String ALLOCATE_PTY = "allocatePty";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_heartbeatInterval">the online documentation</a>
     */
    public static final String HEARTBEAT_INTERVAL = "heartbeatInterval";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_heartbeatInterval">the online documentation</a>
     */
    public static final int HEARTBEAT_INTERVAL_DEFAULT = 0;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_connectionType">the online documentation</a>
     */
    public static final String CONNECTION_TYPE = "connectionType";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_deleteDirectoryCommand">the online documentation</a>
     */
    public static final String DELETE_DIRECTORY_COMMAND = "deleteDirectoryCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_deleteDirectoryCommand">the online documentation</a>
     */
    public static final String DELETE_DIRECTORY_COMMAND_DEFAULT = "rmdir {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_deleteFileCommand">the online documentation</a>
     */
    public static final String DELETE_FILE_COMMAND = "deleteFileCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_deleteFileCommand">the online documentation</a>
     */
    public static final String DELETE_FILE_COMMAND_DEFAULT = "rm -f {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_deleteRecursivelyCommand">the online documentation</a>
     */
    public static final String DELETE_RECURSIVELY_COMMAND = "deleteRecursivelyCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_deleteRecursivelyCommand">the online documentation</a>
     */
    public static final String DELETE_RECURSIVELY_COMMAND_DEFAULT = "rm -rf {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_getFileInfoCommand">the online documentation</a>
     */
    public static final String GET_FILE_INFO_COMMAND = "getFileInfoCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_getFileInfoCommand">the online documentation</a>
     */
    public static final String GET_FILE_INFO_COMMAND_DEFAULT = "ls -ld {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_interactiveKeyboardAuthRegex">the online documentation</a>
     */
    public static final String INTERACTIVE_KEYBOARD_AUTH_PROMPT_REGEX = "interactiveKeyboardAuthRegex";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_interactiveKeyboardAuthRegex">the online documentation</a>
     */
    public static final String INTERACTIVE_KEYBOARD_AUTH_PROMPT_REGEX_DEFAULT = ".*Password:[ ]?";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_listFilesCommand">the online documentation</a>
     */
    public static final String LIST_FILES_COMMAND = "listFilesCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_listFilesCommand">the online documentation</a>
     * <em>NOTE:</em>: this *is* meant to be 'el es minus one'. Each file should go on a separate line, even if we create a pseudo-tty. Long format
     * is NOT what we want here.
     */
    public static final String LIST_FILES_COMMAND_DEFAULT = "ls -a1 {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_mkdirCommand">the online documentation</a>
     */
    public static final String MKDIR_COMMAND = "mkdirCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_mkdirCommand">the online documentation</a>
     */
    public static final String MKDIR_COMMAND_DEFAULT = "mkdir {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_mkdirsCommand">the online documentation</a>
     */
    public static final String MKDIRS_COMMAND = "mkdirsCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_mkdirsCommand">the online documentation</a>
     */
    public static final String MKDIRS_COMMAND_DEFAULT = "mkdir -p {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_openShellBeforeExecute">the online documentation</a>
     */
    public static final String OPEN_SHELL_BEFORE_EXECUTE = "openShellBeforeExecute";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_openShellBeforeExecute">the online documentation</a>
     */
    public static final boolean OPEN_SHELL_BEFORE_EXECUTE_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_passphrase">the online documentation</a>
     */
    public static final String PASSPHRASE = registerFilteredKey("passphrase");

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_passphrase">the online documentation</a>
     */
    public static final String PRIVATE_KEY = registerFilteredKey("privateKey");

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_passphrase">the online documentation</a>
     */
    public static final String PRIVATE_KEY_FILE = "privateKeyFile";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_portAllocationRangeStart">the online documentation</a>
     */
    public static final String PORT_ALLOCATION_RANGE_START = "portAllocationRangeStart";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_portAllocationRangeStart">the online documentation</a>
     */
    public static final int PORT_ALLOCATION_RANGE_START_DEFAULT = 1024;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_renameToCommand">the online documentation</a>
     */
    public static final String RENAME_TO_COMMAND = "renameToCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_renameToCommand">the online documentation</a>
     */
    public static final String RENAME_TO_COMMAND_DEFAULT = "mv {0} {1}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_setExecutableCommand">the online documentation</a>
     */
    public static final String SET_EXECUTABLE_COMMAND = "setExecutableCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_setExecutableCommand">the online documentation</a>
     */
    public static final String SET_EXECUTABLE_COMMAND_DEFAULT = "chmod a+x {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_setNotExecutableCommand">the online documentation</a>
     */
    public static final String SET_NOT_EXECUTABLE_COMMAND = "setNotExecutableCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_setNotExecutableCommand">the online documentation</a>
     */
    public static final String SET_NOT_EXECUTABLE_COMMAND_DEFAULT = "chmod a-x {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suCommandPrefix">the online documentation</a>
     */
    public static final String SU_COMMAND_PREFIX = "suCommandPrefix";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suCommandPrefix">the online documentation</a>
     */
    public static final String SU_COMMAND_PREFIX_DEFAULT = "su - {0} -c";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suCopyFromTempFileCommand">the online documentation</a>
     */
    public static final String SU_COPY_FROM_TEMP_FILE_COMMAND = "suCopyFromTempFileCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suCopyFromTempFileCommand">the online documentation</a>
     */
    public static final String SU_COPY_FROM_TEMP_FILE_COMMAND_DEFAULT_PRESERVE_ATTRIBUTES = "cp -pr {0} {1}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suCopyFromTempFileCommand">the online documentation</a>
     */
    public static final String SU_COPY_FROM_TEMP_FILE_COMMAND_DEFAULT_NO_PRESERVE_ATTRIBUTES = "cp -r {0} {1}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suCopyToTempFileCommand">the online documentation</a>
     */
    public static final String SU_COPY_TO_TEMP_FILE_COMMAND = "suCopyToTempFileCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suCopyToTempFileCommand">the online documentation</a>
     */
    public static final String SU_COPY_TO_TEMP_FILE_COMMAND_DEFAULT_PRESERVE_ATTRIBUTES = "cp -pr {0} {1}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suCopyToTempFileCommand">the online documentation</a>
     */
    public static final String SU_COPY_TO_TEMP_FILE_COMMAND_DEFAULT_NO_PRESERVE_ATTRIBUTES = "cp -r {0} {1}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suOverrideUmask">the online documentation</a>
     */
    public static final String SU_OVERRIDE_UMASK = "suOverrideUmask";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suOverrideUmask">the online documentation</a>
     */
    public static final boolean SU_OVERRIDE_UMASK_DEFAULT = true;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suOverrideUmaskCommand">the online documentation</a>
     */
    public static final String SU_OVERRIDE_UMASK_COMMAND = "suOverrideUmaskCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suOverrideUmaskCommand">the online documentation</a>
     */
    public static final String SU_OVERRIDE_UMASK_COMMAND_DEFAULT = "chmod -R go+rX {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suPassword">the online documentation</a>
     */
    public static final String SU_PASSWORD = registerFilteredKey("suPassword");

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suPasswordPromptRegex">the online documentation</a>
     */
    public static final String SU_PASSWORD_PROMPT_REGEX = "suPasswordPromptRegex";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suPasswordPromptRegex">the online documentation</a>
     */
    public static final String SU_PASSWORD_PROMPT_REGEX_DEFAULT = ".*[Pp]assword.*:";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suPreserveAttributesOnCopyFromTempFile">the online documentation</a>
     */
    public static final String SU_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE = "suPreserveAttributesOnCopyFromTempFile";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suPreserveAttributesOnCopyFromTempFile">the online documentation</a>
     */
    public static final boolean SU_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suPreserveAttributesOnCopyToTempFile">the online documentation</a>
     */
    public static final String SU_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE = "suPreserveAttributesOnCopyToTempFile";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suPreserveAttributesOnCopyToTempFile">the online documentation</a>
     */
    public static final boolean SU_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suQuoteCommand">the online documentation</a>
     */
    public static final String SU_QUOTE_COMMAND = "suQuoteCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suQuoteCommand">the online documentation</a>
     */
    public static final boolean SU_QUOTE_COMMAND_DEFAULT = true;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suTempMkdirCommand">the online documentation</a>
     */
    public static final String SU_TEMP_MKDIR_COMMAND = "suTempMkdirCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suTempMkdirCommand">the online documentation</a>
     */
    public static final String SU_TEMP_MKDIR_COMMAND_DEFAULT = "mkdir -m 1777 {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suTempMkdirsCommand">the online documentation</a>
     */
    public static final String SU_TEMP_MKDIRS_COMMAND = "suTempMkdirsCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suTempMkdirCommand">the online documentation</a>
     */
    public static final String SU_TEMP_MKDIRS_COMMAND_DEFAULT = "mkdir -p -m 1777 {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_suUsername">the online documentation</a>
     */
    public static final String SU_USERNAME = "suUsername";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoCommandPrefix">the online documentation</a>
     */
    public static final String SUDO_COMMAND_PREFIX = "sudoCommandPrefix";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoCommandPrefix">the online documentation</a>
     */
    public static final String SUDO_COMMAND_PREFIX_DEFAULT = "sudo -u {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoCopyFromTempFileCommand">the online documentation</a>
     */
    public static final String SUDO_COPY_FROM_TEMP_FILE_COMMAND = "sudoCopyFromTempFileCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoCopyFromTempFileCommand">the online documentation</a>
     */
    public static final String SUDO_COPY_FROM_TEMP_FILE_COMMAND_DEFAULT_PRESERVE_ATTRIBUTES = "cp -pr {0} {1}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoCopyFromTempFileCommand">the online documentation</a>
     */
    public static final String SUDO_COPY_FROM_TEMP_FILE_COMMAND_DEFAULT_NO_PRESERVE_ATTRIBUTES = "cp -r {0} {1}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoCopyToTempFileCommand">the online documentation</a>
     */
    public static final String SUDO_COPY_TO_TEMP_FILE_COMMAND = "sudoCopyToTempFileCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoCopyToTempFileCommand">the online documentation</a>
     */
    public static final String SUDO_COPY_TO_TEMP_FILE_COMMAND_DEFAULT_PRESERVE_ATTRIBUTES = "cp -pr {0} {1}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoCopyToTempFileCommand">the online documentation</a>
     */
    public static final String SUDO_COPY_TO_TEMP_FILE_COMMAND_DEFAULT_NO_PRESERVE_ATTRIBUTES = "cp -r {0} {1}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoOverrideUmask">the online documentation</a>
     */
    public static final String SUDO_OVERRIDE_UMASK = "sudoOverrideUmask";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoOverrideUmask">the online documentation</a>
     */
    public static final boolean SUDO_OVERRIDE_UMASK_DEFAULT = true;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoOverrideUmaskCommand">the online documentation</a>
     */
    public static final String SUDO_OVERRIDE_UMASK_COMMAND = "sudoOverrideUmaskCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoOverrideUmaskCommand">the online documentation</a>
     */
    public static final String SUDO_OVERRIDE_UMASK_COMMAND_DEFAULT = "chmod -R go+rX {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoPasswordPromptRegex">the online documentation</a>
     */
    public static final String SUDO_PASSWORD_PROMPT_REGEX = "sudoPasswordPromptRegex";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoPasswordPromptRegex">the online documentation</a>
     */
    public static final String SUDO_PASSWORD_PROMPT_REGEX_DEFAULT = ".*[Pp]assword.*:";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoPreserveAttributesOnCopyFromTempFile">the online documentation</a>
     */
    public static final String SUDO_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE = "sudoPreserveAttributesOnCopyFromTempFile";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoPreserveAttributesOnCopyFromTempFile">the online documentation</a>
     */
    public static final boolean SUDO_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoPreserveAttributesOnCopyToTempFile">the online documentation</a>
     */
    public static final String SUDO_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE = "sudoPreserveAttributesOnCopyToTempFile";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoPreserveAttributesOnCopyToTempFile">the online documentation</a>
     */
    public static final boolean SUDO_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoQuoteCommand">the online documentation</a>
     */
    public static final String SUDO_QUOTE_COMMAND = "sudoQuoteCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoQuoteCommand">the online documentation</a>
     */
    public static final boolean SUDO_QUOTE_COMMAND_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoTempMkdirCommand">the online documentation</a>
     */
    public static final String SUDO_TEMP_MKDIR_COMMAND = "sudoTempMkdirCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoTempMkdirCommand">the online documentation</a>
     */
    public static final String SUDO_TEMP_MKDIR_COMMAND_DEFAULT = "mkdir -m 1777 {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoTempMkdirsCommand">the online documentation</a>
     */
    public static final String SUDO_TEMP_MKDIRS_COMMAND = "sudoTempMkdirsCommand";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoTempMkdirsCommand">the online documentation</a>
     */
    public static final String SUDO_TEMP_MKDIRS_COMMAND_DEFAULT = "mkdir -p -m 1777 {0}";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_sudoUsername">the online documentation</a>
     */
    public static final String SUDO_USERNAME = "sudoUsername";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_localAddress">the online
     * documentation</a>
     */
    public static final String LOCAL_ADDRESS = "localAddress";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#ssh_localPort">the online
     * documentation</a>
     */
    public static final String LOCAL_PORT = "localPort";

    protected SshConnection connection;

    public SshConnectionBuilder(String type, ConnectionOptions options, AddressPortMapper mapper) {
        SshConnectionType sshConnectionType = options.getEnum(CONNECTION_TYPE, SshConnectionType.class);

        switch (sshConnectionType) {
            case SFTP:
                connection = new SshSftpUnixConnection(type, options, mapper);
                break;
            case SFTP_CYGWIN:
                connection = new SshSftpCygwinConnection(type, options, mapper);
                break;
            case SFTP_WINSSHD:
                connection = new SshSftpWinSshdConnection(type, options, mapper);
                break;
            case SCP:
                connection = new SshScpConnection(type, options, mapper);
                break;
            case SU:
                connection = new SshSuConnection(type, options, mapper);
                break;
            case SUDO:
                connection = new SshSudoConnection(type, options, mapper);
                break;
            case INTERACTIVE_SUDO:
                connection = new SshInteractiveSudoConnection(type, options, mapper);
                break;
            default:
                throw new IllegalArgumentException("Unknown SSH connection type " + sshConnectionType);
        }
    }

    @Override
    public OverthereConnection connect() {
        connection.connect();
        return connection;
    }

    @Override
    public String toString() {
        return connection.toString();
    }

}
