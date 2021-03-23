package com.xebialabs.overthere.gcp;

/**
 * Generate SSH key pair
 */
interface GenerateSshKey {

    /**
     * Generate SSH key pair for input username and key size.
     *
     * @param username username that will be part of generated key
     * @param keySize size of the key that will be generated
     * @return key pair (public and private key)
     */
    SshKeyPair generate(String username, int keySize);
}
