package com.xebialabs.overthere.cifs.winrm.connector;

import org.apache.http.auth.AuthScheme;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.params.HttpParams;

class WsmanSPNegoSchemeFactory extends SPNegoSchemeFactory {

    public WsmanSPNegoSchemeFactory(boolean stripPort) {
        super(stripPort);
    }
    
    public AuthScheme newInstance(final HttpParams params) {
        return new WsmanSPNegoScheme(isStripPort());
    }

}
