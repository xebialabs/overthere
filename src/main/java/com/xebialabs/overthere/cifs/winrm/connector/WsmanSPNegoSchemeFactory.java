package com.xebialabs.overthere.cifs.winrm.connector;

import org.apache.http.auth.AuthScheme;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.params.HttpParams;

class WsmanSPNegoSchemeFactory extends SPNegoSchemeFactory {

    private final String spnServiceClass;

    private final String spnHost;

    private final int spnPort;

    public WsmanSPNegoSchemeFactory(boolean stripPort, final String spnServiceClass, final String spnHost, final int spnPort) {
        super(stripPort);
        this.spnServiceClass = spnServiceClass;
        this.spnHost = spnHost;
        this.spnPort = spnPort;
    }
    
    public AuthScheme newInstance(final HttpParams params) {
        return new WsmanSPNegoScheme(isStripPort(), spnServiceClass, spnHost, spnPort);
    }

}
