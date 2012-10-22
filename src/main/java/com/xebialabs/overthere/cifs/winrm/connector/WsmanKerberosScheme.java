package com.xebialabs.overthere.cifs.winrm.connector;

import org.apache.http.impl.auth.KerberosScheme;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

class WsmanKerberosScheme extends KerberosScheme {

    public WsmanKerberosScheme(boolean stripPort) {
        super(stripPort);
    }
    
    @Override
    protected byte[] generateGSSToken(
        final byte[] input, final Oid oid, final String authServer) throws GSSException {
        byte[] token = input;
        if (token == null) {
            token = new byte[0];
        }
        GSSManager manager = getManager();
        GSSName serverName = manager.createName("WSMAN@" + authServer, GSSName.NT_HOSTBASED_SERVICE);
        GSSContext gssContext = manager.createContext(
            serverName.canonicalize(oid), oid, null, GSSContext.DEFAULT_LIFETIME);
        gssContext.requestMutualAuth(true);
        gssContext.requestCredDeleg(true);
        return gssContext.initSecContext(token, 0, token.length);
    }

}
