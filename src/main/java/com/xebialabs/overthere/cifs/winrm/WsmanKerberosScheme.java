/*
 * Copyright (c) 2008-2013, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere.cifs.winrm;

import org.apache.http.impl.auth.KerberosScheme;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WsmanKerberosScheme extends KerberosScheme {

    private final String spnServiceClass;

    private final String spnAddress;

    private final int spnPort;

    public WsmanKerberosScheme(final boolean stripPort, final String spnServiceClass, final String spnAddress, final int spnPort) {
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

        String spn = spnServiceClass + "@" + authServer;

        logger.debug("Requesting Kerberos ticket for SPN {}", spn);
        GSSManager manager = getManager();
        GSSName serverName = manager.createName(spn, GSSName.NT_HOSTBASED_SERVICE);
        GSSName canonicalizedName = serverName.canonicalize(oid);

        logger.debug("Creating Kerberos GSS context for canonicalized SPN {}", canonicalizedName);
        GSSContext gssContext = manager.createContext(canonicalizedName, oid, null, GSSContext.DEFAULT_LIFETIME);
        gssContext.requestMutualAuth(true);
        gssContext.requestCredDeleg(true);
        return gssContext.initSecContext(token, 0, token.length);
    }

    private static final Logger logger = LoggerFactory.getLogger(WsmanKerberosScheme.class);

}
