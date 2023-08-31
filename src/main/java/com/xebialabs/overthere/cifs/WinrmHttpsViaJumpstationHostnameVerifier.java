package com.xebialabs.overthere.cifs;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class WinrmHttpsViaJumpstationHostnameVerifier implements HostnameVerifier {
    String remoteHost;
    HostnameVerifier hostnameVerifier;

    public WinrmHttpsViaJumpstationHostnameVerifier(String remoteHost, HostnameVerifier hostnameVerifier) {
        this.remoteHost = remoteHost;
        this.hostnameVerifier = hostnameVerifier;
    }

    @Override
    public boolean verify(final String host, final SSLSession session) {
        return this.hostnameVerifier.verify(remoteHost, session);
    }
}
