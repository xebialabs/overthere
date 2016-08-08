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
package com.xebialabs.overthere.smb2;

import com.hierynomus.smbj.DefaultConfig;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.Share;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import com.xebialabs.overthere.proxy.ProxyConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.BaseOverthereConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.smb2.Smb2ConnectionBuilder.*;
import static java.net.InetSocketAddress.createUnresolved;

public class Smb2Connection extends BaseOverthereConnection {

    private final SMBClient client;
    private final String hostname;
    private final int smbPort;
    private final String domain;
    private final String shareName;
    private Connection connection;
    private Session session;
    private DiskShare share;
    protected final String password;

    protected CifsConnectionType cifsConnectionType;
    protected final String username;
    private int port;

    protected Smb2Connection(String protocol, ConnectionOptions options, AddressPortMapper mapper, boolean canStartProcess) {
        super(protocol, options, mapper, canStartProcess);
        this.cifsConnectionType = options.getEnum(CONNECTION_TYPE, CifsConnectionType.class);
        if (mapper instanceof ProxyConnection) {
            throw new IllegalArgumentException("Cannot open a smb2:" + cifsConnectionType.toString().toLowerCase() + ": connection through an HTTP proxy");
        }

        String unmappedAddress = options.get(ADDRESS);

        int unmappedPort = options.get(PORT, this.cifsConnectionType.getDefaultPort(options));
        InetSocketAddress addressPort = mapper.map(createUnresolved(unmappedAddress, unmappedPort));
        hostname = addressPort.getHostName();
        port = addressPort.getPort();

        int unmappedSmbPort = options.getInteger(SMB2_PORT, PORT_DEFAULT_SMB2);
        InetSocketAddress smbAddressPort = mapper.map(createUnresolved(unmappedAddress, unmappedSmbPort));
        smbPort = smbAddressPort.getPort();
        username = options.get(USERNAME);
        password = options.get(PASSWORD);
        domain = options.getOptional(DOMAIN);
        shareName = options.get(SHARE);
        client = new SMBClient(new DefaultConfig());
    }

    public void connect() {
        try {
            connection = client.connect(hostname);
            AuthenticationContext authContext = new AuthenticationContext(username, password.toCharArray(), domain);
            session = connection.authenticate(authContext);
            Share share = session.connectShare(shareName);
            if (!(share instanceof DiskShare)) {
                close();
                throw new RuntimeIOException("The share " + shareName + " is not a disk share");
            }
            this.share = (DiskShare) share;
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
        connected();
    }


    @Override
    public OverthereFile getFile(String hostPath) {
        return new Smb2File(this, hostPath);
    }

    @Override
    public OverthereFile getFile(OverthereFile parent, String child) {
        return parent.getFile(child);
    }

    @Override
    protected void doClose() {
        try {
            share.close();
        } catch (IOException e) {
            logger.warn("Exception while trying to close smb2 share", e);
        } finally {
            try {
                session.close();
            } catch (IOException e) {
                logger.warn("Exception while trying to close smb2 session", e);
            } finally {

                try {
                    connection.close();
                } catch (Exception e) {
                    logger.warn("Exception while trying to close smb2 connection", e);
                }
            }
        }
    }

    @Override
    protected OverthereFile getFileForTempFile(OverthereFile parent, String name) {
        return getFile(parent, name);
    }

    @Override
    public String toString() {
        return "smb:" + cifsConnectionType.toString().toLowerCase() + "://" + username + "@" + hostname + ":" + smbPort + ":" + port;
    }

    DiskShare getShare() {
        return share;
    }

    private static Logger logger = LoggerFactory.getLogger(Smb2Connection.class);
}
