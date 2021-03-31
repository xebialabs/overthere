package com.xebialabs.overthere.gcp;

/**
 * Google Cloud Platform key manager that caches keys for expiry period. During key validity period it allows usage of the same key.
 * After expiry period it generates and registers new key pair on the GCP.
 */
interface GcpKeyManager {

    /**
     * Initialize this instance
     *
     * @return this instance
     */
    GcpKeyManager init();

    /**
     * Refresh key after expiry period, if there is no current key it generates new one.
     *
     * @param expiryInMs Key expiry period in milliseconds
     * @param keySize Key size
     * @return generated private key with username for current expiry period
     */
    GcpSshKey refreshKey(long expiryInMs, int keySize);
}
