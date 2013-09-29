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

import static java.lang.String.format;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.Closeables.closeQuietly;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_ENABLE_HTTPS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRS_ALLOW_DELEGATE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRS_COMPRESSION;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRS_NOECHO;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRS_NOPROFILE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRS_PROXY_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRS_UNENCRYPTED;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_ENABLE_HTTPS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_ALLOW_DELEGATE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_COMPRESSION;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_NOECHO;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_NOPROFILE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_PROXY_CONNECTION_OPTIONS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_PROXY_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRS_UNENCRYPTED;

public class CifsWinrsConnection extends CifsConnection {

    private ConnectionOptions options;

    private OverthereConnection winrsProxyConnection;

    public CifsWinrsConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper, true);
        checkArgument(os == WINDOWS, "Cannot start a " + CIFS_PROTOCOL + ":%s connection to a machine that is not running Windows", cifsConnectionType.toString().toLowerCase());
        checkArgument(!username.contains("\\"), "Cannot start a " + CIFS_PROTOCOL + ":%s connection with an old-style Windows domain account [%s], use USER@DOMAIN instead.", cifsConnectionType.toString().toLowerCase(), username);
        checkArgument(mapper instanceof DefaultAddressPortMapper, "Cannot create a " + CIFS_PROTOCOL + ":%s connection when connecting through a SSH jumpstation", cifsConnectionType.toString().toLowerCase());

        this.options = options;

        connectToWinrsProxy(options);
        
        if(winrsProxyConnection.getHostOperatingSystem() != WINDOWS) {
            disconnectFromWinrsProxy();
            throw new IllegalArgumentException(format("Cannot create a " + CIFS_PROTOCOL + ":%s connection with a winrs proxy that is not running Windows", cifsConnectionType.toString().toLowerCase()));
        }
    }

    @Override
    public void doClose() {
        disconnectFromWinrsProxy();
    }

    private void connectToWinrsProxy(ConnectionOptions options) {
        logger.debug("Connecting to winrs proxy");

        String winrsProxyProtocol = options.get(WINRS_PROXY_PROTOCOL, DEFAULT_WINRS_PROXY_PROTOCOL);
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
        if (options.getBoolean(WINRS_NOECHO, DEFAULT_WINRS_NOECHO)) {
            winrsCmd.addArgument("-noecho");
        }
        if (options.getBoolean(WINRS_NOPROFILE, DEFAULT_WINRS_NOPROFILE)) {
            winrsCmd.addArgument("-noprofile");
        }
        if (options.getBoolean(WINRS_ALLOW_DELEGATE, DEFAULT_WINRS_ALLOW_DELEGATE)) {
            winrsCmd.addArgument("-allowdelegate");
        }
        if (options.getBoolean(WINRS_COMPRESSION, DEFAULT_WINRS_COMPRESSION)) {
            winrsCmd.addArgument("-compression");
        }
        if (options.getBoolean(WINRS_UNENCRYPTED, DEFAULT_WINRS_UNENCRYPTED)) {
            winrsCmd.addArgument("-unencrypted");
        }
        if (options.getBoolean(WINRM_ENABLE_HTTPS, DEFAULT_WINRM_ENABLE_HTTPS)) {
            winrsCmd.addArgument("-usessl");
        }
        winrsCmd.add(cmd.getArguments());

        return winrsProxyConnection.startProcess(winrsCmd);
    }

    private static final Logger logger = LoggerFactory.getLogger(CifsWinrsConnection.class);

}
