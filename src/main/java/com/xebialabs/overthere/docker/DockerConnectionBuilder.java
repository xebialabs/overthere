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

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;
import static com.xebialabs.overthere.docker.DockerConnectionBuilder.DOCKER_PROTOCOL;

/**
 * Builds Docker connections.
 */
@Protocol(name =DOCKER_PROTOCOL )
public class DockerConnectionBuilder implements OverthereConnectionBuilder {

    /**
     * Name of the protocol handled by this connection builder, i.e. "docker".
     */
    public static final String DOCKER_PROTOCOL = "docker";

    public static final String CONNECTION_TYPE = "connectionType";

    public static final String DOCKER_HOST = "dockerHost";
    public static final String DOCKER_CERT_PATH = "dockerCertPath";
    public static final String DOCKER_TLS_VERIFY = "dockerTlsVerify";
    public static final String DOCKER_CONTAINER = "dockerContainer";

    public static final int DOCKER_TLS_VERIFY_DEFAULT = 1;

    public static final String DELETE_DIRECTORY_COMMAND = "deleteDirectoryCommand";

    public static final String DELETE_DIRECTORY_COMMAND_DEFAULT = "rmdir {0}";

    public static final String DELETE_FILE_COMMAND = "deleteFileCommand";
    public static final String DELETE_FILE_COMMAND_DEFAULT = "rm -f {0}";

    public static final String DELETE_RECURSIVELY_COMMAND = "deleteRecursivelyCommand";

    public static final String DELETE_RECURSIVELY_COMMAND_DEFAULT = "rm -rf {0}";

    public static final String GET_FILE_INFO_COMMAND = "getFileInfoCommand";

    public static final String GET_FILE_INFO_COMMAND_DEFAULT = "ls -ld {0}";

    public static final String LIST_FILES_COMMAND = "listFilesCommand";

    public static final String LIST_FILES_COMMAND_DEFAULT = "ls -a1 {0}";

    public static final String MKDIR_COMMAND = "mkdirCommand";

    public static final String MKDIR_COMMAND_DEFAULT = "mkdir {0}";

    public static final String MKDIRS_COMMAND = "mkdirsCommand";

    public static final String MKDIRS_COMMAND_DEFAULT = "mkdir -p {0}";

    public static final String RENAME_TO_COMMAND = "renameToCommand";

    public static final String RENAME_TO_COMMAND_DEFAULT = "mv {0} {1}";

    public static final String SET_EXECUTABLE_COMMAND = "setExecutableCommand";

    public static final String SET_EXECUTABLE_COMMAND_DEFAULT = "chmod a+x {0}";

    public static final String SET_NOT_EXECUTABLE_COMMAND = "setNotExecutableCommand";

    public static final String SET_NOT_EXECUTABLE_COMMAND_DEFAULT = "chmod a-x {0}";






    private OverthereConnection connection;

    public DockerConnectionBuilder(String protocol, ConnectionOptions options) {
        this(protocol,options, null);
    }
    public DockerConnectionBuilder(String protocol, ConnectionOptions options, AddressPortMapper mapper) {
        DockerConnectionType connectionType = options.getEnum(CONNECTION_TYPE, DockerConnectionType.class);

        switch (connectionType) {
            case LOCAL_CLIENT:
                connection = new DockerLocalClientConnection(protocol, options);
                break;
            default:
                throw new IllegalArgumentException("Unknown Docker connection type " + connectionType);
        }
    }

    @Override
    public OverthereConnection connect() {
        return connection;
    }

    @Override
    public String toString() {
        return connection.toString();
    }

}
