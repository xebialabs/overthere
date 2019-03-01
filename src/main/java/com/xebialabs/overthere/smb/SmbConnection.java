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
package com.xebialabs.overthere.smb;

import com.hierynomus.mserref.NtStatus;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.security.bc.BCSecurityProvider;
import com.hierynomus.smb.SMBPacket;
import com.hierynomus.smb.SMBPacketData;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.Share;
import com.hierynomus.smbj.transport.TransportLayerFactory;
import com.hierynomus.smbj.transport.tcp.direct.DirectTcpTransportFactory;
import com.hierynomus.smbj.transport.tcp.tunnel.TunnelTransportFactory;
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
import java.util.Map;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.smb.SmbConnectionBuilder.*;
import static java.net.InetSocketAddress.createUnresolved;

public class SmbConnection extends BaseOverthereConnection {

    private final SMBClient client;
    private final String hostname;
    private final String realSmbHost;
    private final int realSmbPort;
    private final int smbPort;
    private Connection connection;
    private Session session;
    private int port;

    protected final String password;
    protected CifsConnectionType cifsConnectionType;
    protected final String username;

    protected SmbConnection(String protocol, ConnectionOptions options, AddressPortMapper mapper, boolean canStartProcess) {
        super(protocol, options, mapper, canStartProcess);
        this.cifsConnectionType = options.getEnum(CONNECTION_TYPE, CifsConnectionType.class);
        if (mapper instanceof ProxyConnection) {
            throw new IllegalArgumentException("Cannot open a smb:" + cifsConnectionType.toString().toLowerCase() + ": connection through an HTTP proxy");
        }

        realSmbHost = options.get(ADDRESS);

        int unmappedPort = options.get(PORT, this.cifsConnectionType.getDefaultPort(options));
        InetSocketAddress addressPort = mapper.map(createUnresolved(realSmbHost, unmappedPort));
        hostname = addressPort.getHostName();
        port = addressPort.getPort();

        realSmbPort = options.getInteger(SMB_PORT, PORT_DEFAULT_SMB);
        InetSocketAddress smbAddressPort = mapper.map(createUnresolved(realSmbHost, realSmbPort));
        smbPort = smbAddressPort.getPort();
        boolean requireSigning = options.getBoolean(SMB_REQUIRE_SIGNING, SMB_REQUIRE_SIGNING_DEFAULT);
        TransportLayerFactory<SMBPacketData<?>, SMBPacket<?, ?>> transportLayerFactory = new DirectTcpTransportFactory<>();
        if (!realSmbHost.equals(hostname)) {
            transportLayerFactory = new TunnelTransportFactory<>(transportLayerFactory, hostname, smbPort);
        }
        username = options.get(USERNAME);
        password = options.get(PASSWORD);
        SmbConfig config = SmbConfig.builder()
                .withSigningRequired(requireSigning)
                .withTransportLayerFactory(transportLayerFactory)
                .withSecurityProvider(new BCSecurityProvider())
                .build();
        client = new SMBClient(config);
    }

    public void connect() {
        createConnection();
        connected();
    }

    private void createConnection() {
        try {
            UserAndDomain ud = getUserNameAndDomain(username);
            String user = ud.getUsername();
            String domain = ud.getDomain();
            connection = client.connect(realSmbHost, realSmbPort);
            AuthenticationContext authContext = new AuthenticationContext(user, password.toCharArray(), domain);
            session = connection.authenticate(authContext);
        } catch (SMBApiException smbApi) {
            if (smbApi.getStatus() == NtStatus.STATUS_LOGON_FAILURE) {
                throw new RuntimeIOException(smbApi);
            }
            throw smbApi;
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    @Override
    public OverthereFile getFile(String hostPath) {
        Map<String, String> pathMappings = options.get(PATH_SHARE_MAPPINGS, PATH_SHARE_MAPPINGS_DEFAULT);
        return new SmbFile(this, hostPath, pathMappings);
    }

    @Override
    public OverthereFile getFile(OverthereFile parent, String child) {
        return parent.getFile(child);
    }

    @Override
    protected void doClose() {
        try {
            if (session != null) {
                session.close();
            }
        } catch (IOException e) {
            logger.warn("Exception while trying to close smb session", e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                logger.warn("Exception while trying to close smb connection", e);
            } finally {
                try {
                    if (client != null) {
                        client.close();
                    }
                } catch (Exception e) {
                    logger.warn("Exception closing the SMB Client", e);
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

    protected DiskShare getShare(String shareName) {
        Share share = session.connectShare(shareName);
        if (!(share instanceof DiskShare)) {
            close();
            throw new RuntimeIOException("The share " + shareName + " is not a disk share");
        }
        return (DiskShare) share;
    }

    private UserAndDomain getUserNameAndDomain(String user) {
        if (user.contains("\\")) {
            String[] split = user.split("\\\\");
            return new UserAndDomain(split[1], split[0]);
        }
        if (user.contains("@")) {
            String[] split = user.split("@");
            return new UserAndDomain(split[0], split[1]);
        }
        return new UserAndDomain(user, "");
    }

    private static class UserAndDomain {
        String username;
        String domain;

        private UserAndDomain(String username, String domain) {
            this.username = username;
            this.domain = domain;
        }

        private String getUsername() {
            return username;
        }

        private String getDomain() {
            return domain;
        }
    }

    private static Logger logger = LoggerFactory.getLogger(SmbConnection.class);
}
