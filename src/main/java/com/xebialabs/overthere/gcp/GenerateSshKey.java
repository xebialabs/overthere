package com.xebialabs.overthere.gcp;

interface GenerateSshKey {

    SshKeyPair generate(String username, int keySize);
}
