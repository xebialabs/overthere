package com.xebialabs.overthere.cifs.winrs;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.xebialabs.overthere.*;
import com.xebialabs.overthere.cifs.CifsConnection;
import com.xebialabs.overthere.cifs.CifsConnectionBuilder;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

import static com.google.common.collect.Iterables.transform;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE;
import static java.lang.String.format;

public class CifsWinRsConnection extends CifsConnection {

    private String winrsExecFile = "C:\\WINDOWS\\system32\\winrs.exe";
    private CifsConnectionType winrsConnectionType;
    private URL targetURL;

    public CifsWinRsConnection(String type, ConnectionOptions options) {
        super(type, options, false);
        this.winrsConnectionType = options.get(CONNECTION_TYPE);
        this.port = options.get(PORT, getDefaultPort());
        this.targetURL = getTargetURL(options);
    }

    @Override
    public OverthereProcess startProcess(CmdLine commandLine) {
        logger.debug("startProcess : {} ", commandLine.toCommandLine(os, true));

        /*
         * winrs -remote:https://host:port
         * -username:[domain\]username -password:passwd -directory:temporaryDirectoryPath script_to_execute
         */
        final ImmutableList<String> fullCommandLine = new ImmutableList.Builder<String>()
                .add(winrsExecFile)
                .add(format("-remote:%s", targetURL.toString()))
                .add(format("-username:%s", username))
                .add(format("-password:%s", password))
                .add(format("-directory:%s", temporaryDirectoryPath))
                .addAll(transform(commandLine.getArguments(), new Function<CmdLineArgument, String>() {
                    public String apply(CmdLineArgument input) {
                        return input.getArg();
                    }
                })).build();

        logger.debug("winrs base command line {}", Joiner.on(" ").join(fullCommandLine));
        return new WinrsOverthereProcess(fullCommandLine);
    }

    private Integer getDefaultPort() {
        switch (winrsConnectionType) {
            case WINRS_HTTP:
                return CifsConnectionBuilder.DEFAULT_WINRM_HTTP_PORT;
            case WINRS_HTTPS:
                return CifsConnectionBuilder.DEFAULT_WINRM_HTTPS_PORT;
            default:
                throw new IllegalArgumentException("Unknown Winrs connection type " + winrsConnectionType);
        }
    }

    private URL getTargetURL(ConnectionOptions options) {
        String scheme = winrsConnectionType == CifsConnectionType.WINRS_HTTP ? "http" : "https";
        try {
            return new URL(scheme, address, port, "");
        } catch (MalformedURLException e) {
            throw new RuntimeIOException("Cannot build a new URL for " + this, e);
        }
    }

    @Override
    public String toString() {
        return "winrs:" + winrsConnectionType + "://" + username + "@" + address + ":" + port;
    }

    private static Logger logger = LoggerFactory.getLogger(CifsConnection.class);
}
