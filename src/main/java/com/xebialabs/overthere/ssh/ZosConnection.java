package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SCP;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZosConnection {
    private static final Logger logger = LoggerFactory.getLogger(OverthereConnector.class);

    private SshConnection sshScpZosConnection;
    private SshConnection sshSftpZosConnection;


    public ZosConnection(ConnectionOptions options) {
        initializeConnections(options);
    }

    private void initializeConnections(ConnectionOptions targetOptions) {
        try {
            targetOptions.set(CONNECTION_TYPE, SCP);
            this.sshScpZosConnection = (SshConnection) Overthere.getConnection("ssh", targetOptions);
        } catch (Exception e) {
            logger.warn("OverThere:zosConnection:SCP Failed", e);
            this.sshScpZosConnection = null;
        }

        try {
            targetOptions.set(CONNECTION_TYPE, SFTP);
            this.sshSftpZosConnection = (SshConnection) Overthere.getConnection("ssh", targetOptions);
        } catch (Exception e) {
            logger.warn("OverThere:zosConnection:SFTP Failed", e);
            this.sshSftpZosConnection = null;
        }
    }

    public OverthereConnection getConnectionForScp() { return sshScpZosConnection; }

    public OverthereConnection getConnectionForSftp() {
        return sshSftpZosConnection;
    }

    public OverthereConnection getConnection(SshConnectionType connectionType) {
        if (connectionType.equals(SFTP)) {
            return sshSftpZosConnection;
        } else if (connectionType.equals(SCP)) {
            return sshScpZosConnection;
        } else {
            throw new IllegalArgumentException("Unsupported connection type: " + connectionType);
        }
    }

    public ZosConnection getConnection() {
       return this;
    }
}