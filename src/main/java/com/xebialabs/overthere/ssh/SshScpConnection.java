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

import java.util.List;
import com.google.common.base.Splitter;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler;

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.OperatingSystemFamily.ZOS;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.*;
import static com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.NullOverthereExecutionOutputHandler.swallow;

/**
 * A connection to a Unix host using SSH w/ SCP.
 */
class SshScpConnection extends SshConnection {

    protected String deleteDirectoryCommand;
    protected String deleteFileCommand;

    protected String deleteRecursivelyCommand;

    protected String getFileInfoCommand;

    protected String getUserGroupsCommand;

    protected String listFilesCommand;

    protected String mkdirCommand;

    protected String mkdirsCommand;

    protected String renameToCommand;

    protected String setExecutableCommand;

    protected String setNotExecutableCommand;

    public SshScpConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper);
        checkArgument(os != WINDOWS, "Cannot create a " + SSH_PROTOCOL + ":%s connection to a Windows operating system", sshConnectionType.toString().toLowerCase());
        checkArgument(os != ZOS, "Cannot create a " + SSH_PROTOCOL + ":%s connection to a z/OS operating system", sshConnectionType.toString().toLowerCase());

        deleteDirectoryCommand = options.get(DELETE_DIRECTORY_COMMAND, DELETE_DIRECTORY_COMMAND_DEFAULT);
        deleteFileCommand = options.get(DELETE_FILE_COMMAND, DELETE_FILE_COMMAND_DEFAULT);
        deleteRecursivelyCommand = options.get(DELETE_RECURSIVELY_COMMAND, DELETE_RECURSIVELY_COMMAND_DEFAULT);
        getFileInfoCommand = options.get(GET_FILE_INFO_COMMAND, GET_FILE_INFO_COMMAND_DEFAULT);
        getUserGroupsCommand = options.get(GET_USER_GROUP_DETAILS_COMMAND, GET_USER_GROUP_DETAILS_COMMAND_DEFAULT);
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

    /**
     * Gets the group names the logged in user belongs to.
     * @return
     */
    protected GroupDetails getUserGroups() {
        CmdLine cmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND).addArgument(getUserGroupsCommand);
        CapturingOverthereExecutionOutputHandler stdoutHandler = capturingHandler();
        execute(stdoutHandler, swallow(), cmdLine);
        return new GroupDetails(stdoutHandler.getOutput());
    }

    static class GroupDetails {
        private List<String> groups;

        public GroupDetails(String groups) {
            this.groups = Splitter.on(" ").splitToList(groups);
        }

        public List<String> getGroups() {
            return groups;
        }
    }
}
