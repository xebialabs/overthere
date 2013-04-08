package com.xebialabs.overthere.cifs.winrm.connector;

import org.apache.http.impl.auth.SPNegoScheme;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

class WsmanSPNegoScheme extends SPNegoScheme {

    private final String spnServiceClass;

    private final String spnAddress;

    private final int spnPort;

    public WsmanSPNegoScheme(final boolean stripPort, final String spnServiceClass, final String spnAddress, final int spnPort) {
        super(stripPort);
        this.spnServiceClass = spnServiceClass;
        this.spnAddress = spnAddress;
        this.spnPort = spnPort;
    }
    
    @Override
    protected byte[] generateGSSToken(final byte[] input, final Oid oid, String authServer) throws GSSException {
        byte[] token = input;
        if (token == null) {
            token = new byte[0];
        }

        if(authServer.equals("localhost")) {
            if(authServer.indexOf(':') > 0) {
                authServer = spnAddress + ":" + spnPort;
            } else {
                authServer = spnAddress;
            }
        }

        GSSManager manager = getManager();
        GSSName serverName = manager.createName(spnServiceClass + "@" + authServer, GSSName.NT_HOSTBASED_SERVICE);
        GSSContext gssContext = manager.createContext(serverName.canonicalize(oid), oid, null, GSSContext.DEFAULT_LIFETIME);
        gssContext.requestMutualAuth(true);
        gssContext.requestCredDeleg(true);
        return gssContext.initSecContext(token, 0, token.length);
    }

}
