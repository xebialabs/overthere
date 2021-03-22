package com.xebialabs.overthere.gcp;

public class GcpSshKey {

    private final SshKeyPair sshKeyPair;
    private final String username;
    private final long expirationTimeUsec;

    public GcpSshKey(final SshKeyPair sshKeyPair, final String username, final long expirationTimeUsec) {
        this.sshKeyPair = sshKeyPair;
        this.username = username;
        this.expirationTimeUsec = expirationTimeUsec;
    }

    public long getExpirationTimeUsec() {
        return expirationTimeUsec;
    }

    public String getPrivateKey() {
        return sshKeyPair.getPrivateKey();
    }

    public String getUsername() {
        return username;
    }
}
