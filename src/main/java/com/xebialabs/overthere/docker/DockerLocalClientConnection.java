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
package com.xebialabs.overthere.docker;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.local.LocalConnection;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;

import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static com.xebialabs.overthere.util.OverthereUtils.checkNotNull;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;

import static com.xebialabs.overthere.docker.DockerConnectionBuilder.*;
import static java.lang.String.format;

/**
 * A connection to a Windows host using CIFS and the Windows native implementation of WinRM, i.e. the <tt>winrs</tt> command.
 */
public class DockerLocalClientConnection extends DockerConnection {

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


    private LocalConnection dockerProxyConnection;

    public DockerLocalClientConnection(String protocol, ConnectionOptions options) {
        super(protocol, options, DefaultAddressPortMapper.INSTANCE, true);

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

        //this.options = options;
        connectToDockerProxy(options);

        // Make sure that we're properly cleaned up by setting the connected state.
        connected();
    }

    @Override
    public void doClose() {
        logger.debug("Disconnecting from native docker client proxy");
        closeQuietly(dockerProxyConnection);
    }

    private void connectToDockerProxy(ConnectionOptions options) {
        logger.debug("Connecting to native docker client proxy");

        //String winrsProxyProtocol = options.get(WINRS_PROXY_PROTOCOL, WINRS_PROXY_PROTOCOL_DEFAULT);
        //ConnectionOptions winrsProxyConnectionOptions = options.get(WINRS_PROXY_CONNECTION_OPTIONS, new ConnectionOptions());
        dockerProxyConnection = (LocalConnection) Overthere.getConnection(LocalConnection.LOCAL_PROTOCOL, new ConnectionOptions());

    }

    protected CmdLine getDockerExecCmd() {
        CmdLine dockerCmd = new CmdLine();
        dockerCmd.addArgument("docker");
        dockerCmd.addArgument("exec");
        dockerCmd.addArgument(dockerContainer);
        return dockerCmd;
    }

    @Override
    public OverthereFile getFile(String hostPath) throws RuntimeIOException {
        return new DockerLocalClientFile(this, hostPath);
    }

    @Override
    public OverthereProcess startProcess(final CmdLine cmd) {
        checkNotNull(cmd, "Cannot execute null command line");
        checkArgument(cmd.getArguments().size() > 0, "Cannot execute empty command line");

        final String obfuscatedCmd = cmd.toCommandLine(os, true);
        logger.info("Starting command [{}] on [{}]", obfuscatedCmd, this);

        CmdLine dockerExecCmd;
        if (getWorkingDirectory() != null) {
            dockerExecCmd = getDockerExecCmd();
            dockerExecCmd.addArgument("sh").addArgument("-c");
            String cmdAsString = cmd.toCommandLine(UNIX, false);
            dockerExecCmd.addRaw("cd " + workingDirectory.getPath() + "; " + cmdAsString);
        } else {
            dockerExecCmd = getDockerExecCmd();
            dockerExecCmd.add(cmd.getArguments());
        }

        return executeDocker(dockerExecCmd);
    }

    public OverthereProcess executeDocker(CmdLine cmd) {
        Map<String,String> env = new HashMap<String, String>();
        env.put("DOCKER_HOST", dockerHost);
        env.put("DOCKER_CERT_PATH", dockerCertPath);
        env.put("DOCKER_TLS_VERIFY", String.valueOf(dockerTlsVerify));
        return dockerProxyConnection.startProcess(cmd, env);
    }

    @Override
    protected int attachToProcess(final OverthereExecutionOutputHandler stdoutHandler, final OverthereExecutionOutputHandler stderrHandler, final CmdLine commandLine, final OverthereProcess process) {
        return super.attachToProcess(stdoutHandler, stderrHandler, commandLine, process);
    }

    private static final Logger logger = LoggerFactory.getLogger(DockerLocalClientConnection.class);

}
