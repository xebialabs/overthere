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
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.BaseOverthereConnection;

import java.io.IOException;
import java.net.InetSocketAddress;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.smb2.Smb2ConnectionBuilder.*;
import static java.net.InetSocketAddress.createUnresolved;

public class Smb2Connection extends BaseOverthereConnection {

    private final SMBClient client;
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;
    private final String domain;
    private final String shareName;
    private Connection connection;
    private Session session;
    private DiskShare share;

    protected Smb2Connection(String protocol, ConnectionOptions options, AddressPortMapper mapper) {
        super(protocol, options, mapper, false);
        String unmappedAddress = options.get(ADDRESS);
        int unmappedPort = options.getInteger(PORT, PORT_DEFAULT_SMB2);
        InetSocketAddress addressPort = mapper.map(createUnresolved(unmappedAddress, unmappedPort));
        hostname = addressPort.getHostName();
        port = addressPort.getPort();
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

        } finally {
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (Exception e) {

                }
            }
        }
    }

    @Override
    protected OverthereFile getFileForTempFile(OverthereFile parent, String name) {
        return null;
    }

    @Override
    public String toString() {
        return null;
    }

    DiskShare getShare() {
        return share;
    }
}
