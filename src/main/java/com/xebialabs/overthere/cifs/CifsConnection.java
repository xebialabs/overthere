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
package com.xebialabs.overthere.cifs;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.xebialabs.overthere.proxy.ProxyConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.BaseOverthereConnection;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PORT_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.PATH_SHARE_MAPPINGS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.PATH_SHARE_MAPPINGS_DEFAULT;
import static java.net.InetSocketAddress.createUnresolved;

/**
 * Base class for connections to a Windows host using CIFS.
 * <p/>
 * Limitations:
 * <ul>
 * <li>Shares with names like C$ need to available for all drives accessed. In practice, this means that Administrator
 * access is needed.</li>
 * <li>Not tested with domain accounts.</li>
 * </ul>
 */
public abstract class CifsConnection extends BaseOverthereConnection {

    protected CifsConnectionType cifsConnectionType;

    protected String unmappedAddress;

    protected int unmappedPort;

    protected String address;

    protected int cifsPort;

    protected int port;

    protected String username;

    protected String password;

    protected PathEncoder encoder;

    protected NtlmPasswordAuthentication authentication;

    /**
     * Creates a {@link CifsConnection}. Don't invoke directly. Use
     * {@link Overthere#getConnection(String, ConnectionOptions)} instead.
     */
    public CifsConnection(String protocol, ConnectionOptions options, AddressPortMapper mapper, boolean canStartProcess) {
        super(protocol, options, mapper, canStartProcess);
        this.cifsConnectionType = options.getEnum(CONNECTION_TYPE, CifsConnectionType.class);
        if(mapper instanceof ProxyConnection) {
            throw new IllegalArgumentException("Cannot open a cifs:" + cifsConnectionType.toString().toLowerCase() + ": connection through an HTTP proxy");
        }
        this.unmappedAddress = options.get(ADDRESS);
        this.unmappedPort = options.get(PORT, this.cifsConnectionType.getDefaultPort(options));
        InetSocketAddress addressPort = mapper.map(createUnresolved(unmappedAddress, unmappedPort));
        this.address = addressPort.getHostName();
        this.port = addressPort.getPort();
        this.username = options.get(USERNAME);
        this.password = options.get(PASSWORD);
        int unmappedCifsPort = options.getInteger(CIFS_PORT, CIFS_PORT_DEFAULT);
        InetSocketAddress addressCifsPort = mapper.map(createUnresolved(unmappedAddress, unmappedCifsPort));
        this.cifsPort = addressCifsPort.getPort();
        this.encoder = new PathEncoder(null, null, this.address, cifsPort, options.get(PATH_SHARE_MAPPINGS, PATH_SHARE_MAPPINGS_DEFAULT));
        this.authentication = new NtlmPasswordAuthentication(null, username, password);
    }

    protected abstract void connect();

    @Override
    public void doClose() {
        // no-op
    }

    @Override
    public OverthereFile getFile(String hostPath) throws RuntimeIOException {
        try {
            SmbFile smbFile = new SmbFile(encodeAsSmbUrl(hostPath), authentication);
            return new CifsFile(this, smbFile);
        } catch (IOException exc) {
            throw new RuntimeIOException(exc);
        }
    }

    @Override
    public OverthereFile getFile(OverthereFile parent, String child) throws RuntimeIOException {
        StringBuilder childPath = new StringBuilder();
        childPath.append(parent.getPath());
        if (!parent.getPath().endsWith(getHostOperatingSystem().getFileSeparator())) {
            childPath.append(getHostOperatingSystem().getFileSeparator());
        }
        childPath.append(child.replace('\\', '/'));
        return getFile(childPath.toString());
    }

    @Override
    protected OverthereFile getFileForTempFile(OverthereFile parent, String name) {
        return getFile(parent, name);
    }

    private String encodeAsSmbUrl(String hostPath) {
        try {
            String smbUrl = encoder.toSmbUrl(hostPath);
            logger.trace("Encoded Windows host path [{}] to SMB URL [{}]", hostPath, maskSmbUrl(smbUrl));
            return smbUrl;
        } catch (IllegalArgumentException exception) {
            throw new RuntimeIOException(exception);
        }
    }

    private String maskSmbUrl(String smbUrl) {
        return smbUrl.replace(password, "********");
    }

    @Override
    public String toString() {
        return "cifs:" + cifsConnectionType.toString().toLowerCase() + "://" + username + "@" + address + ":" + cifsPort + ":" + port;
    }

    private static Logger logger = LoggerFactory.getLogger(CifsConnection.class);

}
