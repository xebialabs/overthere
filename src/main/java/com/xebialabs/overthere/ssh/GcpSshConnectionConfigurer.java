package com.xebialabs.overthere.ssh;

public class GcpSshConnectionConfigurer {

    private final SshConnection sshConnection;

    public GcpSshConnectionConfigurer(final SshConnection sshConnection) {
        this.sshConnection = sshConnection;
    }

    public SshConnection configureSshConnection(String privateKey, String username) {
        sshConnection.privateKey = privateKey;
        sshConnection.username = username;
        return this.sshConnection;
    }
}
