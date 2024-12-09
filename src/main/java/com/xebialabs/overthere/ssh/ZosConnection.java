package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SCP;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;

public class ZosConnection {
    private SshConnection overthereSshScp;
    private SshConnection overthereSshSftp;

    public ZosConnection(ConnectionOptions options) {
        initializeConnections(options);
    }

    private void initializeConnections(ConnectionOptions targetOptions) {
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

    public SshConnection getConnectionForScp() { return overthereSshScp; }

    public SshConnection getConnectionForSftp() {
        return overthereSshSftp;
    }

    public SshConnection getConnection(SshConnectionType connectionType) {
        if (connectionType.equals(SFTP)) {
            return overthereSshSftp;
        } else if (connectionType.equals(SCP)) {
            return overthereSshScp;
        } else {
            throw new IllegalArgumentException("Unsupported connection type: " + connectionType);
        }
    }

    public ZosConnection getConnection() {
       return this;
    }
}