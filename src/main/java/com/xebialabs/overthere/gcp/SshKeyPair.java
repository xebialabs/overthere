package com.xebialabs.overthere.gcp;

final class SshKeyPair {
    private final String keyUsername;
    private final String privateKey;
    private final String publicKey;
    private final String fingerPrint;

    public SshKeyPair(final String keyUsername, final String privateKey, final String publicKey, final String fingerPrint) {
        this.keyUsername = keyUsername;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.fingerPrint = fingerPrint;
    }

    public String getKeyUsername() {
        return keyUsername;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getFingerPrint() {
        return fingerPrint;
    }
}
