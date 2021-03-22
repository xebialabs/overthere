package com.xebialabs.overthere.gcp;

interface GcpKeyManager {

    GcpKeyManager init();

    GcpSshKey refreshKey(long expiryInUsec, int keySize);
}
