package com.xebialabs.overthere.cifs.winrm.connector;

import org.apache.http.auth.AuthScheme;
import org.apache.http.impl.auth.KerberosSchemeFactory;
import org.apache.http.params.HttpParams;

class WsmanKerberosSchemeFactory extends KerberosSchemeFactory {

    public WsmanKerberosSchemeFactory(boolean stripPort) {
        super(stripPort);
    }
    
    public AuthScheme newInstance(final HttpParams params) {
        return new WsmanKerberosScheme(isStripPort());
    }

}
