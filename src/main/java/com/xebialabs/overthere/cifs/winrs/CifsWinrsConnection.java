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
package com.xebialabs.overthere.cifs.winrs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.cifs.CifsConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;

import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static com.xebialabs.overthere.util.OverthereUtils.checkNotNull;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_ENABLE_HTTPS_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRS_ALLOW_DELEGATE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_COMPRESSION_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_NOECHO_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_NOPROFILE_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_PROXY_PROTOCOL_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_UNENCRYPTED_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_ENABLE_HTTPS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_ALLOW_DELEGATE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_COMPRESSION;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_NOECHO;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_NOPROFILE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_PROXY_CONNECTION_OPTIONS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_PROXY_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_UNENCRYPTED;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static java.lang.String.format;

/**
 * A connection to a Windows host using CIFS and the Windows native implementation of WinRM, i.e. the <tt>winrs</tt> command.
 */
public class CifsWinrsConnection extends CifsConnection {

    private ConnectionOptions options;

    private OverthereConnection winrsProxyConnection;

    public CifsWinrsConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper, true);
        checkArgument(os == WINDOWS, "Cannot create a " + CIFS_PROTOCOL + ":%s connection to a machine that is not running Windows", cifsConnectionType.toString().toLowerCase());
        checkArgument(mapper instanceof DefaultAddressPortMapper, "Cannot create a " + CIFS_PROTOCOL + ":%s connection when connecting through a SSH jumpstation", cifsConnectionType.toString().toLowerCase());
        checkArgument(password.indexOf('\'') == -1 && password.indexOf('\"') == -1, "Cannot create a " + CIFS_PROTOCOL + ":%s connection with a password that contains a single quote (\') or a double quote (\")", cifsConnectionType.toString().toLowerCase());

        this.options = options;
    }

    @Override
    public void connect() {
        connectToWinrsProxy(options);

        if (winrsProxyConnection.getHostOperatingSystem() != WINDOWS) {
            disconnectFromWinrsProxy();
            throw new IllegalArgumentException(format("Cannot create a " + CIFS_PROTOCOL + ":%s connection with a winrs proxy that is not running Windows", cifsConnectionType.toString().toLowerCase()));
        }

        connected();
    }

    @Override
    public void doClose() {
        disconnectFromWinrsProxy();
    }

    private void connectToWinrsProxy(ConnectionOptions options) {
        logger.debug("Connecting to winrs proxy");

        String winrsProxyProtocol = options.get(WINRS_PROXY_PROTOCOL, WINRS_PROXY_PROTOCOL_DEFAULT);
        ConnectionOptions winrsProxyConnectionOptions = options.get(WINRS_PROXY_CONNECTION_OPTIONS, new ConnectionOptions());
        winrsProxyConnection = Overthere.getConnection(winrsProxyProtocol, winrsProxyConnectionOptions);
    }

    private void disconnectFromWinrsProxy() {
        logger.debug("Disconnecting from winrs proxy");

        closeQuietly(winrsProxyConnection);
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

    private static final Logger logger = LoggerFactory.getLogger(CifsWinrsConnection.class);

}
