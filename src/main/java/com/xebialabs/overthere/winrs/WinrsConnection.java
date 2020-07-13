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
package com.xebialabs.overthere.winrs;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import com.xebialabs.overthere.spi.ProcessConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.*;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_ENABLE_HTTPS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_ENABLE_HTTPS_DEFAULT;
import static com.xebialabs.overthere.cifs.ConnectionValidator.checkIsWindowsHost;
import static com.xebialabs.overthere.cifs.ConnectionValidator.checkNoSingleQuoteInPassword;
import static com.xebialabs.overthere.cifs.ConnectionValidator.checkNotThroughJumpstation;
import static com.xebialabs.overthere.smb.SmbConnectionBuilder.SMB_PROTOCOL;
import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static com.xebialabs.overthere.util.OverthereUtils.checkNotNull;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static java.lang.String.format;
import static java.net.InetSocketAddress.createUnresolved;

/**
 * A connection to a Windows host using Windows native implementation of WinRM, i.e. the <tt>winrs</tt> command.
 */
public class WinrsConnection implements ProcessConnection {

    private OperatingSystemFamily os;
    private OverthereFile workingDirectory;
    private String address;
    private int port;
    private String password;
    private String username;
    private String protocol;
    private CifsConnectionType connectionType = CifsConnectionType.WINRM_NATIVE;
    private ConnectionOptions options;

    private OverthereConnection winrsProxyConnection;

    public WinrsConnection(ConnectionOptions options, AddressPortMapper mapper, OverthereFile workingDirectory) {
        this.workingDirectory = workingDirectory;
        this.options = options;
        this.os = options.getEnum(OPERATING_SYSTEM, OperatingSystemFamily.class);
        String unmappedAddress = options.get(ADDRESS);
        int unmappedPort = options.get(PORT, connectionType.getDefaultPort(options));
        InetSocketAddress addressPort = mapper.map(createUnresolved(unmappedAddress, unmappedPort));
        this.address = addressPort.getHostName();
        this.port = addressPort.getPort();
        this.username = options.get(USERNAME);
        this.password = options.get(PASSWORD);
        this.protocol = options.get(PROTOCOL);

        checkIsWindowsHost(os, protocol, connectionType);
        checkNotThroughJumpstation(mapper, protocol, connectionType);
        checkNoSingleQuoteInPassword(password, protocol, connectionType);
    }

    @Override
    public void connect() {
        connectToWinrsProxy(options);
        if (winrsProxyConnection.getHostOperatingSystem() != WINDOWS) {
            disconnectFromWinrsProxy();
            throw new IllegalArgumentException(format("Cannot create a " + SMB_PROTOCOL + ":%s connection with a winrs proxy that is not running Windows", connectionType.toString().toLowerCase()));
        }
    }

    @Override
    public OverthereProcess startProcess(final CmdLine cmd) {
        checkNotNull(cmd, "Cannot execute null command line");
        checkArgument(cmd.getArguments().size() > 0, "Cannot execute empty command line");

        final String obfuscatedCmd = cmd.toCommandLine(os, true);
        logger.info("Starting command [{}] on [{}]", obfuscatedCmd, this);

        final CmdLine winrsCmd = new CmdLine();
        winrsCmd.addArgument("winrs");
        winrsCmd.addArgument("-remote:" + address + ":" + port);
        winrsCmd.addArgument("-username:" + username);
        winrsCmd.addPassword("-password:" + password);
        if (workingDirectory != null) {
            winrsCmd.addArgument("-directory:" + workingDirectory.getPath());
        }
        if (options.getBoolean(WINRS_NOECHO, WINRS_NOECHO_DEFAULT)) {
            winrsCmd.addArgument("-noecho");
        }
        if (options.getBoolean(WINRS_NOPROFILE, WINRS_NOPROFILE_DEFAULT)) {
            winrsCmd.addArgument("-noprofile");
        }
        if (options.getBoolean(WINRS_ALLOW_DELEGATE, DEFAULT_WINRS_ALLOW_DELEGATE)) {
            winrsCmd.addArgument("-allowdelegate");
        }
        if (options.getBoolean(WINRS_COMPRESSION, WINRS_COMPRESSION_DEFAULT)) {
            winrsCmd.addArgument("-compression");
        }
        if (options.getBoolean(WINRS_UNENCRYPTED, WINRS_UNENCRYPTED_DEFAULT)) {
            winrsCmd.addArgument("-unencrypted");
        }
        if (options.getBoolean(WINRM_ENABLE_HTTPS, WINRM_ENABLE_HTTPS_DEFAULT)) {
            winrsCmd.addArgument("-usessl");
        }
        winrsCmd.add(cmd.getArguments());

        return winrsProxyConnection.startProcess(winrsCmd);
    }

    @Override
    public void close() {
        disconnectFromWinrsProxy();
    }

    @Override
    public void setWorkingDirectory(OverthereFile workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    OverthereConnection connectToWinrsProxy(ConnectionOptions options) {
        logger.debug("Connecting to winrs proxy");

        String winrsProxyProtocol = options.get(WINRS_PROXY_PROTOCOL, WINRS_PROXY_PROTOCOL_DEFAULT);
        ConnectionOptions winrsProxyConnectionOptions = options.get(WINRS_PROXY_CONNECTION_OPTIONS, new ConnectionOptions());
        winrsProxyConnection = Overthere.getConnection(winrsProxyProtocol, winrsProxyConnectionOptions);
        return winrsProxyConnection;
    }

    void disconnectFromWinrsProxy() {
        logger.debug("Disconnecting from winrs proxy");
        closeQuietly(winrsProxyConnection);
    }

    private static final Logger logger = LoggerFactory.getLogger(WinrsConnection.class);
}
