package com.xebialabs.overthere.cifs.winrm;

import org.apache.http.auth.AuthScheme;
import org.apache.http.impl.auth.KerberosSchemeFactory;
import org.apache.http.params.HttpParams;

class WsmanKerberosSchemeFactory extends KerberosSchemeFactory {

    private final String spnServiceClass;
    private final String spnHost;
    private final int spnPort;
    private final WinRmHttpClient whClient;

    public WsmanKerberosSchemeFactory(final boolean stripPort, final String spnServiceClass, final String spnHost, final int spnPort, WinRmHttpClient whClient) {
        super(stripPort);
        this.spnServiceClass = spnServiceClass;
        this.spnHost = spnHost;
        this.spnPort = spnPort;
        this.whClient = whClient;
    }
    
    public AuthScheme newInstance(final HttpParams params) {
        return new WsmanKerberosScheme(isStripPort(), spnServiceClass, spnHost, spnPort, whClient);
    }

}
