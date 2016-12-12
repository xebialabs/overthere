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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.BaseOverthereConnection;

import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE;

public abstract class DockerConnection extends BaseOverthereConnection {

    protected DockerConnectionType dockerConnectionType;

    protected String dockerHost;

    protected String dockerCertPath;

    protected String dockerContainer;

    protected int dockerTlsVerify;

    public DockerConnection(String protocol, ConnectionOptions options, AddressPortMapper mapper, boolean canStartProcess) {
        super(protocol, options, mapper, canStartProcess);
        this.dockerConnectionType = options.getEnum(CONNECTION_TYPE, DockerConnectionType.class);
        this.dockerHost = options.get(DockerConnectionBuilder.DOCKER_HOST);
        this.dockerCertPath = options.get(DockerConnectionBuilder.DOCKER_CERT_PATH);
        this.dockerContainer = options.get(DockerConnectionBuilder.DOCKER_CONTAINER);
        this.dockerTlsVerify = options.get(DockerConnectionBuilder.DOCKER_TLS_VERIFY, DockerConnectionBuilder.DOCKER_TLS_VERIFY_DEFAULT);
    }


    @Override
    public void doClose() {
        // no-op
    }

    @Override
    public OverthereFile getFile(OverthereFile parent, String child) throws RuntimeIOException {
        StringBuilder childPath = new StringBuilder();
        childPath.append(parent.getPath());
        if (!parent.getPath().endsWith(getHostOperatingSystem().getFileSeparator())) {
            childPath.append(getHostOperatingSystem().getFileSeparator());
        }
        childPath.append(child);
        return getFile(childPath.toString());
    }

    @Override
    protected OverthereFile getFileForTempFile(OverthereFile parent, String name) {
        return getFile(parent, name);
    }

    @Override
    public String toString() {
        return "docker:" + dockerConnectionType.toString().toLowerCase() + ":" + dockerHost + ":" + dockerContainer;
    }

    private static Logger logger = LoggerFactory.getLogger(DockerConnection.class);

}
