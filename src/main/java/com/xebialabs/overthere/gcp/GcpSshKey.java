package com.xebialabs.overthere.gcp;

public final class GcpSshKey {

    private final SshKeyPair sshKeyPair;
    private final String username;
    private final long expirationTimeMs;

    public GcpSshKey(final SshKeyPair sshKeyPair, final String username, final long expirationTimeMs) {
        this.sshKeyPair = sshKeyPair;
        this.username = username;
        this.expirationTimeMs = expirationTimeMs;
    }

    public long getExpirationTimeMs() {
        return expirationTimeMs;
    }

    public String getPrivateKey() {
        return sshKeyPair.getPrivateKey();
    }

    public String getUsername() {
        return username;
    }
}
