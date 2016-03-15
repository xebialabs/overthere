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
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;

import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.OperatingSystemFamily.ZOS;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.DELETE_DIRECTORY_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.DELETE_DIRECTORY_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.DELETE_FILE_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.DELETE_FILE_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.DELETE_RECURSIVELY_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.DELETE_RECURSIVELY_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.GET_FILE_INFO_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.GET_FILE_INFO_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.LIST_FILES_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.LIST_FILES_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.MKDIRS_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.MKDIRS_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.MKDIR_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.MKDIR_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.RENAME_TO_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.RENAME_TO_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SET_EXECUTABLE_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SET_EXECUTABLE_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SET_NOT_EXECUTABLE_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SET_NOT_EXECUTABLE_COMMAND_DEFAULT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;

/**
 * A connection to a Unix host using SSH w/ SCP.
 */
class SshScpConnection extends SshConnection {

    protected String deleteDirectoryCommand;

    protected String deleteFileCommand;

    protected String deleteRecursivelyCommand;

    protected String getFileInfoCommand;

    protected String listFilesCommand;

    protected String mkdirCommand;

    protected String mkdirsCommand;

    protected String renameToCommand;

    protected String setExecutableCommand;

    protected String setNotExecutableCommand;

    public SshScpConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper);
        checkArgument(os != WINDOWS, "Cannot create a %s connection to a host that is running Windows", protocolAndConnectionType);
        checkArgument(os != ZOS, "Cannot create a %s connection to a host that is running z/OS", protocolAndConnectionType);

        deleteDirectoryCommand = options.get(DELETE_DIRECTORY_COMMAND, DELETE_DIRECTORY_COMMAND_DEFAULT);
        deleteFileCommand = options.get(DELETE_FILE_COMMAND, DELETE_FILE_COMMAND_DEFAULT);
        deleteRecursivelyCommand = options.get(DELETE_RECURSIVELY_COMMAND, DELETE_RECURSIVELY_COMMAND_DEFAULT);
        getFileInfoCommand = options.get(GET_FILE_INFO_COMMAND, GET_FILE_INFO_COMMAND_DEFAULT);
        listFilesCommand = options.get(LIST_FILES_COMMAND, LIST_FILES_COMMAND_DEFAULT);
        mkdirCommand = options.get(MKDIR_COMMAND, MKDIR_COMMAND_DEFAULT);
        mkdirsCommand = options.get(MKDIRS_COMMAND, MKDIRS_COMMAND_DEFAULT);
        renameToCommand = options.get(RENAME_TO_COMMAND, RENAME_TO_COMMAND_DEFAULT);
        setExecutableCommand = options.get(SET_EXECUTABLE_COMMAND, SET_EXECUTABLE_COMMAND_DEFAULT);
        setNotExecutableCommand = options.get(SET_NOT_EXECUTABLE_COMMAND, SET_NOT_EXECUTABLE_COMMAND_DEFAULT);
    }

    @Override
    public OverthereFile getFile(String hostPath) throws RuntimeIOException {
        return new SshScpFile(this, hostPath);
    }

}
