package com.xebialabs.overthere.cifs.winrs;

import static com.google.common.collect.Iterables.transform;
import static java.lang.String.format;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.CmdLineArgument;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.cifs.CifsConnection;
import com.xebialabs.overthere.cifs.CifsConnectionBuilder;
import com.xebialabs.overthere.cifs.CifsConnectionType;

public class CifsWinRsConnection extends CifsConnection {

    private String winrsExecFile = "C:\\WINDOWS\\system32\\winrs.exe";
    private URL targetURL;

    public CifsWinRsConnection(String type, ConnectionOptions options) {
        super(type, options, false);
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

    @Override
    protected Integer getDefaultPort() {
        switch (cifsConnectionType) {
            case WINRS_HTTP:
                return CifsConnectionBuilder.DEFAULT_WINRM_HTTP_PORT;
            case WINRS_HTTPS:
                return CifsConnectionBuilder.DEFAULT_WINRM_HTTPS_PORT;
            default:
                throw new IllegalArgumentException("Unknown Winrs connection type " + cifsConnectionType);
        }
    }

    private URL getTargetURL(ConnectionOptions options) {
        String scheme = cifsConnectionType == CifsConnectionType.WINRS_HTTP ? "http" : "https";
        try {
            return new URL(scheme, address, port, "");
        } catch (MalformedURLException e) {
            throw new RuntimeIOException("Cannot build a new URL for " + this, e);
        }
    }

    @Override
    public String toString() {
        return "winrs:" + cifsConnectionType + "://" + username + "@" + address + ":" + port;
    }

    private static Logger logger = LoggerFactory.getLogger(CifsConnection.class);
}
