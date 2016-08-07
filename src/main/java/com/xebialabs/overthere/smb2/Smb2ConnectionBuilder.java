package com.xebialabs.overthere.smb2;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import com.xebialabs.overthere.cifs.ConnectionBuilder;
import com.xebialabs.overthere.smb2.telnet.Smb2TelnetConnection;
import com.xebialabs.overthere.smb2.winrm.Smb2WinRmConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

import static com.xebialabs.overthere.smb2.Smb2ConnectionBuilder.SMB2_PROTOCOL;

@Protocol(name = SMB2_PROTOCOL)
public class Smb2ConnectionBuilder extends ConnectionBuilder implements OverthereConnectionBuilder {
    public static final String SMB2_PROTOCOL = "SMB2";

    /**
     * The default port for SMB2 connections over TCP/IP
     */
    public static final int PORT_DEFAULT_SMB2 = 445;

    /**
     * The Windows Domain to authenticate the user against. If not set, bla bla bla
     */
    public static final String DOMAIN = "domain";

    public static final String SHARE = "share";

    private final Smb2Connection connection;

    public Smb2ConnectionBuilder(String type, ConnectionOptions options, AddressPortMapper mapper) {
        CifsConnectionType cifsConnectionType = options.getEnum(CONNECTION_TYPE, CifsConnectionType.class);

        switch (cifsConnectionType) {
            case TELNET:
                connection = new Smb2TelnetConnection(type, options, mapper);
                break;
            case WINRM_INTERNAL:
                connection = new Smb2WinRmConnection(type, options, mapper);
                break;
            case WINRM_NATIVE:
                connection = new Smb2WinRmConnection(type, options, mapper);
                break;
            default:
                throw new IllegalArgumentException("Unknown CIFS connection type " + cifsConnectionType);
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
