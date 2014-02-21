/*
 * Copyright (c) 2008-2013, XebiaLabs B.V., All rights reserved.
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
     * Connection option (Boolean) that specifies whether a default pty ( <code>dummy:80:24:0:0</code>) should be
     * allocated when executing a command. All sudo implementations require it for interactive sudo, some even require
     * it for normal sudo. Some SSH server implementations (notably OpenSSH on AIX 5.3) crash when it is allocated.
     */
    public static final String ALLOCATE_DEFAULT_PTY = "allocateDefaultPty";

    /**
     * Default value (<code>false</code>) for the connection option that specifies whether a default pty should be
     * allocated when executing a command.
     */
    public static final boolean ALLOCATE_DEFAULT_PTY_DEFAULT = false;

    /**
     * Connection option (String) that specifies a specific pty that should be allocated when executing a command. The
     * format is TERM:COLS:ROWS:WIDTH:HEIGTH e.g. <code>xterm:80:24:0:0</code>. If <code>null</code> or an empty string
     * is specified, no pty is allocated. Overrides the {@link #ALLOCATE_DEFAULT_PTY} option.
     */
    public static final String ALLOCATE_PTY = "allocatePty";

    /**
     * Connection option (<code>SshConnectionType</code>) that specifies the {@link SshConnectionType SSH connection
     * type} to use.
     */
    public static final String CONNECTION_TYPE = "connectionType";

    /**
     * Default value (<code>22</code>) for the connection option that specifies the port to connect to.
     */
    public static final int SSH_PORT_DEFAULT = 22;

    /**
     * Connection option (String) that specifies which regular expression to look for in keyboard-interactive prompts
     * before sending the password.
     */
    public static final String INTERACTIVE_KEYBOARD_AUTH_PROMPT_REGEX = "interactiveKeyboardAuthRegex";

    /**
     * Default value (<code>.*Password:[ ]?</code>) for the connection option that specifies which regular expression to
     * look for in keyboard-interactive prompts before sending the password.
     */
    public static final String INTERACTIVE_KEYBOARD_AUTH_PROMPT_REGEX_DEFAULT = ".*Password:[ ]?";

    /**
     * Connection option (String) that specifies the passphrase of the private key.
     */
    public static final String PASSPHRASE = "passphrase";

    /**
     * Connection option (String) that specifies the private key file to use. <b>N.B.:</b> Private keys cannot be used
     * when the SSH connection type is {@link SshConnectionType#INTERACTIVE_SUDO INTERACTIVE_SUDO} because the password
     * is needed for the password prompts.
     */
    public static final String PRIVATE_KEY_FILE = "privateKeyFile";

    /**
     * Connection option (Integer) that specifies from what port onwards to locate an empty port for a local port
     * forward in an {@link SshTunnelConnection tunnel}
     */
    public static final String PORT_ALLOCATION_RANGE_START = "portAllocationRangeStart";

    /**
     * Default value (<code>1024</code>) for the connection option that specifies from what port onwards to locate an
     * empty port for a local port forward in an {@link SshTunnelConnection tunnel}
     */
    public static final int PORT_ALLOCATION_RANGE_START_DEFAULT = 1024;

    /**
     * Connection option (String) that specifies the sudo command to prefix. The placeholder {0} is replaced with the
     * value of {@link #SUDO_USERNAME}.
     */
    public static final String SUDO_COMMAND_PREFIX = "sudoCommandPrefix";

    /**
     * Default value (<code>sudo -u {0}</code>) for the connection option that specifies the sudo command to prefix.
     */
    public static final String SUDO_COMMAND_PREFIX_DEFAULT = "sudo -u {0}";

    /**
     * Connection option (String) that specifies the username to sudo to for {@link SshConnectionType#SUDO SUDO} and
     * {@link SshConnectionType#INTERACTIVE_SUDO INTERACTIVE_SUDO} SSH connections.
     */
    public static final String SUDO_USERNAME = "sudoUsername";

    /**
     * Connection option (String) that specifies an alternate password to use for the password prompt for
     * {@link SshConnectionType#INTERACTIVE_SUDO INTERACTIVE_SUDO} SSH connections. When empty, the default password used for making the connection is used.
     */
    public static final String SUDO_INTERACTIVE_PASSWORD = "sudoInteractivePassword";

    /**
     * Connection option (Boolean) that specifies whether or not to explicitly change the permissions with chmod -R
     * go+rX after uploading a file or directory with scp. Also see {@link #SUDO_OVERRIDE_UMASK_COMMAND}.
     */
    public static final String SUDO_OVERRIDE_UMASK = "sudoOverrideUmask";

    /**
     * Default value (<code>true</code>) for the connection option that specifies whether or not to explicitly change
     * the permissions with go+rX after uploading a file with scp.
     */
    public static final boolean SUDO_OVERRIDE_UMASK_DEFAULT = true;

    /**
     * Connection option (String) that specifies the regular expression to use when looking for in the output stream in
     * interactive sudo before sending the password.
     */
    public static final String SUDO_PASSWORD_PROMPT_REGEX = "sudoPasswordPromptRegex";

    /**
     * Default value (<code>.*[Pp]assword.*:</code>) for the connection option that specifies which regular expression
     * to look for in interactive sudo before sending the password.
     */
    public static final String SUDO_PASSWORD_PROMPT_REGEX_DEFAULT = ".*[Pp]assword.*:";

    /**
     * Connection option (Boolean) that specifies whether files are copied <em>from</em> the connection temporary
     * directory using the <code>-p</code> flag to the <code>cp</code> command.
     */
    public static final String SUDO_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE = "sudoPreserveAttributesOnCopyFromTempFile";

    /**
     * Default value (<code>true</code>) for the connection option that specifies whether files are copied <em>from</em>
     * the connection temporary directory using the <code>-p</code> flag to the <code>cp</code> command.
     */
    public static final boolean SUDO_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE_DEFAULT = true;

    /**
     * Connection option (Boolean) that specifies whether files are copied <em>to</em> the connection temporary
     * directory using the <code>-p</code> flag to the <code>cp</code> command.
     */
    public static final String SUDO_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE = "sudoPreserveAttributesOnCopyToTempFile";

    /**
     * Default value (<code>true</code>) for the connection option that specifies whether files are copied <em>to</em>
     * the connection temporary directory using the <code>-p</code> flag to the <code>cp</code> command.
     */
    public static final boolean SUDO_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE_DEFAULT = true;

    /**
     * Connection option (Boolean) that specifies whether or not to quote the original command when it is prefixed with
     * {@link #SUDO_COMMAND_PREFIX}.
     */
    public static final String SUDO_QUOTE_COMMAND = "sudoQuoteCommand";

    /**
     * Default value (<code>false</code>) for the connection option that specifies whether or not to quote the original
     * command.
     */
    public static final boolean SUDO_QUOTE_COMMAND_DEFAULT = false;

    /**
     * Connection option (Boolean) that specifies whether or not to open a shell directly before executing a remote
     * command. This may be necessary in case a user does not yet have a home directory on a machine, and this is
     * created for him when he opens a shell.
     */
    public static final String OPEN_SHELL_BEFORE_EXECUTE = "openShellBeforeExecute";

    /**
     * Default value (<code>false</code>) for the connection option that specifies whether or not to open a shell
     * directory before executing a command.
     */
    public static final boolean OPEN_SHELL_BEFORE_EXECUTE_DEFAULT = false;

    /**
     * Connection option (String) that specifies the command used to delete a directory when using an SSH/SCP
     * connection. The placeholder <code>{0}</code> is replaced with the directory to delete.
     */
    public static final String DELETE_DIRECTORY_COMMAND = "deleteDirectoryCommand";

    /**
     * Default value (<code>rmdir {0}</code>) for the connection option that specifies the command used to delete a
     * directory using an SSH/SCP connection.
     */
    public static final String DELETE_DIRECTORY_COMMAND_DEFAULT = "rmdir {0}";

    /**
     * Connection option (String) that specifies the command used to delete a file when using an SSH/SCP connection. The
     * placeholder <code>{0}</code> is replaced with the file to delete.
     */
    public static final String DELETE_FILE_COMMAND = "deleteFileCommand";

    /**
     * Default value (<code>rm -f {0}</code>) for the connection option that specifies the command used to delete a file
     * using an SSH/SCP connection.
     */
    public static final String DELETE_FILE_COMMAND_DEFAULT = "rm -f {0}";

    /**
     * Connection option (String) that specifies the command used to delete recursively when using an SSH/SCP
     * connection. The placeholder <code>{0}</code> is replaced with the directory/file to delete recursively.
     */
    public static final String DELETE_RECURSIVELY_COMMAND = "deleteRecursivelyCommand";

    /**
     * Default value (<code>rm -rf {0}</code>) for the connection option that specifies the command used to delete
     * recursively using an SSH/SCP connection.
     */
    public static final String DELETE_RECURSIVELY_COMMAND_DEFAULT = "rm -rf {0}";

    /**
     * Connection option (String) that specifies the command used to get file information when using an SSH/SCP
     * connection. The placeholder <code>{0}</code> is replaced with the file/directory to stat.
     */
    public static final String GET_FILE_INFO_COMMAND = "getFileInfoCommand";

    /**
     * Default value (<code>ls -ld {0}</code>) for the connection option that specifies the command used to get file
     * information using an SSH/SCP connection.
     */
    public static final String GET_FILE_INFO_COMMAND_DEFAULT = "ls -ld {0}";

    /**
     * Connection option (String) that specifies the command used to list files in a directory when using an SSH/SCP
     * connection. The placeholder <code>{0}</code> is replaced with the directory to list.
     */
    public static final String LIST_FILES_COMMAND = "listFilesCommand";

    /**
     * Default value (<code>ls -a1 {0}</code>) for the connection option that specifies the command used to list files
     * in a directory using an SSH/SCP connection. <em>NOTE:</em>: this *is* meant to be 'el es minus one'. Each file
     * should go on a separate line, even if we create a pseudo-tty. Long format is NOT what we want here.
     */
    public static final String LIST_FILES_COMMAND_DEFAULT = "ls -a1 {0}";

    /**
     * Connection option (String) that specifies the command used to create a directory when using an SSH/SCP
     * connection. The placeholder <code>{0}</code> is replaced with the directory to create.
     */
    public static final String MKDIR_COMMAND = "mkdirCommand";

    /**
     * Default value (<code>mkdir {0}</code>) for the connection option that specifies the command used to create a
     * directory using an SSH/SCP connection.
     */
    public static final String MKDIR_COMMAND_DEFAULT = "mkdir {0}";

    /**
     * Connection option (String) that specifies the command used to create a directory tree when using an SSH/SCP
     * connection. The placeholder <code>{0}</code> is replaced with the directory to create.
     */
    public static final String MKDIRS_COMMAND = "mkdirsCommand";

    /**
     * Default value (<code>mkdir -p {0}</code>) for the connection option that specifies the command used to create a
     * directory tree using an SSH/SCP connection.
     */
    public static final String MKDIRS_COMMAND_DEFAULT = "mkdir -p {0}";

    /**
     * Connection option (String) that specifies the command used to rename a file/directory when using an SSH/SCP
     * connection. The placeholder <code>{0}</code> is replaced with the file/directory to rename, and the placeholder
     * <code>{1}</code> with the destination name.
     */
    public static final String RENAME_TO_COMMAND = "renameToCommand";

    /**
     * Default value (<code>mv {0} {1}</code>) for the connection option that specifies the command used to rename a
     * file/directory using an SSH/SCP connection.
     */
    public static final String RENAME_TO_COMMAND_DEFAULT = "mv {0} {1}";

    /**
     * Connection option (String) that specifies the command used to set the executable bit of a file/directory when
     * using an SSH/SCP connection. The placeholder <code>{0}</code> is replaced with the file/directory to make
     * executable.
     */
    public static final String SET_EXECUTABLE_COMMAND = "setExecutableCommand";

    /**
     * Default value (<code>chmod a+x {0}</code>) for the connection option that specifies the command used to make a
     * file/directory executable using an SSH/SCP connection.
     */
    public static final String SET_EXECUTABLE_COMMAND_DEFAULT = "chmod a+x {0}";

    /**
     * Connection option (String) that specifies the command used to unset the executable bit of a file/directory when
     * using an SSH/SCP connection. The placeholder <code>{0}</code> is replaced with the file/directory to make not
     * executable.
     */
    public static final String SET_NOT_EXECUTABLE_COMMAND = "setNotExecutableCommand";

    /**
     * Default value (<code>chmod a-x {0}</code>) for the connection option that specifies the command used to make a
     * file/directory not executable using an SSH/SCP connection.
     */
    public static final String SET_NOT_EXECUTABLE_COMMAND_DEFAULT = "chmod a-x {0}";

    /**
     * Connection option (String) that specifies the command used to create a temporary directory when using an SSH/SUDO
     * connection. The placeholder <code>{0}</code> is replaced with the directory to create. <em>Note:</em> For SUDO
     * access, temporary dirs also need to be writable to the connecting user, otherwise an SCP copy will fail. 1777 is
     * world writable with the sticky bit set.
     */
    public static final String SUDO_TEMP_MKDIR_COMMAND = "sudoTempMkdirCommand";

    /**
     * Default value (<code>mkdir -m 1777 {0}</code>) for the connection option that specifies the command used to
     * create a temporary directory using an SSH/SUDO connection.
     */
    public static final String SUDO_TEMP_MKDIR_COMMAND_DEFAULT = "mkdir -m 1777 {0}";

    /**
     * Connection option (String) that specifies the command used to create a temporary directory tree when using an
     * SSH/SUDO connection. The placeholder <code>{0}</code> is replaced with the directory to create. <em>Note:</em>
     * For SUDO access, temporary dirs also need to be writable to the connecting user, otherwise an SCP copy will fail.
     * 1777 is world writable with the sticky bit set.
     */
    public static final String SUDO_TEMP_MKDIRS_COMMAND = "sudoTempMkdirsCommand";

    /**
     * Default value (<code>mkdir -p -m 1777 {0}</code>) for the connection option that specifies the command used to
     * create a temporary directory using an SSH/SUDO connection.
     */
    public static final String SUDO_TEMP_MKDIRS_COMMAND_DEFAULT = "mkdir -p -m 1777 {0}";

    /**
     * Connection option (String) that specifies the command used to copy files from the connection temporary directory
     * when using an SSH/SUDO connection.
     */
    public static final String SUDO_COPY_FROM_TEMP_FILE_COMMAND = "sudoCopyFromTempFileCommand";

    /**
     * Default value (<code>cp -pr {0} {1}</code>) for the connection option that specifies the command used to copy
     * files from the connection temporary directory while preserving the file attributes using an SSH/SUDO connection.
     * See also {@link #SUDO_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE}.
     */
    public static final String SUDO_COPY_FROM_TEMP_FILE_COMMAND_DEFAULT_PRESERVE_ATTRIBUTES = "cp -pr {0} {1}";

    /**
     * Default value (<code>cp -r {0} {1}</code>) for the connection option that specifies the command used to copy
     * files from the connection temporary directory while not preserving the file attributes using an SSH/SUDO
     * connection. See also {@link #SUDO_PRESERVE_ATTRIBUTES_ON_COPY_FROM_TEMP_FILE}.
     */
    public static final String SUDO_COPY_FROM_TEMP_FILE_COMMAND_DEFAULT_NO_PRESERVE_ATTRIBUTES = "cp -r {0} {1}";

    /**
     * Connection option (String) that specifies the command used to override the umask after copying a file or
     * directory when using an SSH/SUDO connection. The placeholder <code>{0}</code> is replaced with the copied
     * file/directory.
     */
    public static final String SUDO_OVERRIDE_UMASK_COMMAND = "sudoOverrideUmaskCommand";

    /**
     * Default value (<code>chmod -R go+rX {0}</code>) for the connection option that specifies the command used to
     * override the umask after copying a file or directory when using an SSH/SUDO connection.
     */
    public static final String SUDO_OVERRIDE_UMASK_COMMAND_DEFAULT = "chmod -R go+rX {0}";

    /**
     * Connection option (String) that specifies the command used to copy files to the connection temporary directory
     * when using an SSH/SUDO connection.
     */
    public static final String SUDO_COPY_TO_TEMP_FILE_COMMAND = "sudoCopyToTempFileCommand";

    /**
     * Default value (<code>cp -pr {0} {1}</code>) for the connection option that specifies the command used to copy
     * files to the connection temporary directory while preserving the file attributes using an SSH/SUDO connection.
     * See {@link #SUDO_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE}.
     */
    public static final String SUDO_COPY_TO_TEMP_FILE_COMMAND_DEFAULT_PRESERVE_ATTRIBUTES = "cp -pr {0} {1}";

    /**
     * Default value (<code>cp -r {0} {1}</code>) for the connection option that specifies the command used to copy
     * files to the connection temporary directory while not preserving the file attributes using an SSH/SUDO
     * connection. See {@link #SUDO_PRESERVE_ATTRIBUTES_ON_COPY_TO_TEMP_FILE}.
     */
    public static final String SUDO_COPY_TO_TEMP_FILE_COMMAND_DEFAULT_NO_PRESERVE_ATTRIBUTES = "cp -r {0} {1}";

    protected SshConnection connection;

    public SshConnectionBuilder(String type, ConnectionOptions options, AddressPortMapper mapper) {
        SshConnectionType sshConnectionType = options.getEnum(CONNECTION_TYPE, SshConnectionType.class);

        switch (sshConnectionType) {
        case TUNNEL:
            connection = new SshTunnelConnection(type, options, mapper);
            break;
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
