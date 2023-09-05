package com.xebialabs.overthere.cifs;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Use this verifier when establishing secured connection via jumpstation.
 */
public class JumpstationAwareHostnameVerifier implements HostnameVerifier {
    String remoteHost;
    HostnameVerifier hostnameVerifier;

    public JumpstationAwareHostnameVerifier(String remoteHost, HostnameVerifier hostnameVerifier) {
        this.remoteHost = remoteHost;
        this.hostnameVerifier = hostnameVerifier;
    }

    @Override
    public boolean verify(final String host, final SSLSession session) {
        return this.hostnameVerifier.verify(remoteHost, session);
    }
}
