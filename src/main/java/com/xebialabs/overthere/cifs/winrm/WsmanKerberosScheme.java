package com.xebialabs.overthere.cifs.winrm;

import org.apache.http.impl.auth.MyGGSSchemeBase;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WsmanKerberosScheme extends MyGGSSchemeBase {

    private final String spnServiceClass;
    private final String spnAddress;
    private final int spnPort;
    private final WinRmHttpClient whClient;

    private static final String KERBEROS_OID = "1.2.840.113554.1.2.2";

    public WsmanKerberosScheme(final boolean stripPort, final String spnServiceClass, final String spnAddress, final int spnPort, final WinRmHttpClient whClient) {
        super(stripPort);
        this.spnServiceClass = spnServiceClass;
        this.spnAddress = spnAddress;
        this.spnPort = spnPort;
        this.whClient = whClient;
    }

    @Override
    protected byte[] generateToken(final byte[] input, final String authServer) throws GSSException {
        return generateGSSToken(input, new Oid(KERBEROS_OID), authServer);
    }

    protected byte[] generateGSSToken(final byte[] input, final Oid oid, String authServer) throws GSSException {
        byte[] token = input;
        if (token == null) {
            token = new byte[0];
        }

        if (authServer.equals("localhost")) {
            if (authServer.indexOf(':') > 0) {
                authServer = spnAddress + ":" + spnPort;
            } else {
                authServer = spnAddress;
            }
        }

        String spn = spnServiceClass + "@" + authServer;

        logger.debug("Requesting Kerberos ticket for SPN [{}]", spn);
        GSSManager manager = getManager();
        GSSName serverName = manager.createName(spn, GSSName.NT_HOSTBASED_SERVICE);
        GSSName canonicalizedName = serverName.canonicalize(oid);

        logger.debug("Creating Kerberos GSS context for canonicalized SPN [{}]", canonicalizedName);
        GSSContext gssContext = manager.createContext(canonicalizedName, oid, null, GSSContext.DEFAULT_LIFETIME);
        whClient.setGSSContext(gssContext);
        gssContext.requestMutualAuth(true);
        gssContext.requestCredDeleg(true);
        gssContext.requestConf(true);
        return gssContext.initSecContext(token, 0, token.length);
    }

    public String getSchemeName() {
        return "Kerberos";
    }

    @Override
    public String getParameter(String name) {
        return null;
    }

    @Override
    public String getRealm() {
        return null;
    }

    @Override
    public boolean isConnectionBased() {
        return true;
    }

    private static final Logger logger = LoggerFactory.getLogger(WsmanKerberosScheme.class);

}
