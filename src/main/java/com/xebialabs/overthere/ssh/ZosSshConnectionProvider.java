package com.xebialabs.overthere.ssh;

import java.io.IOException;
import java.util.Objects;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SCP;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;

import com.xebialabs.overthere.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZosSshConnectionProvider {
    public static void main(String[] args) {
        ConnectionOptions options = new ConnectionOptions();
        options.set(ADDRESS, "172.18.61.12");
        options.set(USERNAME, "ubuntu");
        options.set(PASSWORD, "devopsqe@123");
        options.set(OPERATING_SYSTEM, UNIX);

        ZosConnection zosConnection = new ZosConnection(options);
        System.out.println(zosConnection.getOverthereForScp());
        System.out.println(zosConnection.getOverthereForSftp());
        zosConnection.checkConnection();
        zosConnection.checkConnection(SshConnectionType.SFTP);
    }
}

class ZosConnection {
    private static final Logger logger = LoggerFactory.getLogger(ZosConnection.class);
    private SshConnection overthereSshScp;
    private SshConnection overthereSshSftp;

    public ZosConnection(ConnectionOptions targetOptions) {
        try {
            ConnectionOptions scpOptions = new ConnectionOptions(targetOptions);
            scpOptions.set(CONNECTION_TYPE, SCP);
            this.overthereSshScp = (SshConnection) Overthere.getConnection("ssh", scpOptions);
        } catch (Exception e) {
            this.overthereSshScp = null;
        }

        try {
            ConnectionOptions sftpOptions = new ConnectionOptions(targetOptions);
            sftpOptions.set(CONNECTION_TYPE, SFTP);
            this.overthereSshSftp = (SshConnection) Overthere.getConnection("ssh", sftpOptions);
        } catch (Exception e) {
            this.overthereSshSftp = null;
        }
    }

    public SshConnection getOverthereForScp() {
        return overthereSshScp;
    }

    public SshConnection getOverthereForSftp() {
        return overthereSshSftp;
    }

    public SshConnection getOverthere(SshConnectionType connectionType) {
        if (connectionType.equals(SFTP)) {
            return overthereSshSftp;
        } else if (connectionType.equals(SCP)) {
            return overthereSshScp;
        } else {
            throw new IllegalArgumentException("Unsupported connection type: " + connectionType);
        }
    }

    public void checkConnection(SshConnectionType connectionType){
        if (connectionType.equals(SFTP)) {
            if (Objects.isNull(overthereSshSftp)) {
                throw new IllegalArgumentException("No SFTP connection available");
            }
            echoTmpDirContents(overthereSshSftp);
        } else if (connectionType.equals(SCP)) {
            if (Objects.isNull(overthereSshScp)) {
                throw new IllegalArgumentException("No SCP connection available");
            }
            echoTmpDirContents(overthereSshScp);
        } else {
            throw new IllegalArgumentException("Unsupported connection type: " + connectionType);
        }
    }

    public void checkConnection(){
        if (Objects.nonNull(overthereSshSftp)) echoTmpDirContents(overthereSshSftp);
        if (Objects.nonNull(overthereSshScp)) echoTmpDirContents(overthereSshScp);
        if (Objects.isNull(overthereSshSftp) && Objects.isNull(overthereSshScp)) {
            throw new IllegalArgumentException("No connection available");
        }
    }

    public void close() throws IOException {
        if (Objects.nonNull(overthereSshSftp)) {
            overthereSshSftp.close();
        }
        if (Objects.nonNull(overthereSshScp)) {
            overthereSshScp.close();
        }
    }

    private void echoTmpDirContents(SshConnection connection) {
        System.out.println("Listing the contents of the temporary directory using "+ connection.protocolAndConnectionType +" connection on host " + connection.host);
        CmdLine cmdLine = new CmdLine();
        cmdLine.addArgument("ls");
        String tempDir = connection.getHostOperatingSystem().getDefaultTemporaryDirectoryPath();
        cmdLine.addArgument(tempDir);
        int returnCode = connection.execute(cmdLine);
        if (returnCode != 0) {
            System.out.println("Failed to execute command for connection type "+ connection.protocolAndConnectionType +" on host" + connection.host +". Return code was ["+ returnCode +"]. Please check the logs.");
        }
        else {
            System.out.println("Successfully executed commands on " + connection.host + ".");
        }
    }
}