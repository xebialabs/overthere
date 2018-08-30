/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
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
package com.xebialabs.overthere.winrm;

import com.xebialabs.overthere.cifs.BaseCifsConnectionBuilder;
import org.apache.http.auth.AuthScheme;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WsmanSPNegoSchemeFactory extends SPNegoSchemeFactory {

    private final String spnServiceClass;

    private final String spnHost;

    private final int spnPort;

    private final boolean useCanonicalHostname;

    public WsmanSPNegoSchemeFactory(boolean stripPort, final String spnServiceClass, final String spnHost, final int spnPort) {
        this(stripPort, spnServiceClass, spnHost, spnPort, BaseCifsConnectionBuilder.WINRM_USE_CANONICAL_HOSTNAME_DEFAULT);
    }

    public WsmanSPNegoSchemeFactory(boolean stripPort, final String spnServiceClass, final String spnHost, final int spnPort, final boolean useCanonicalHostname) {
        super(stripPort, useCanonicalHostname);
        this.spnServiceClass = spnServiceClass;
        this.spnHost = spnHost;
        this.spnPort = spnPort;
        this.useCanonicalHostname = useCanonicalHostname;
    }

    @Override
    public AuthScheme newInstance(final HttpParams params) {
        logger.trace("WsmanSPNegoSchemeFactory.newInstance invoked for SPN {}/{} (spnPort = {}, stripPort = {})", new Object[] {spnServiceClass, spnHost, spnPort, isStripPort() });
        return new WsmanSPNegoScheme(isStripPort(), spnServiceClass, spnHost, spnPort, useCanonicalHostname);
    }

    @Override
    public AuthScheme create(final HttpContext context) {
        logger.trace("WsmanSPNegoSchemeFactory.create invoked for SPN {}/{} (spnPort = {}, stripPort = {})", new Object[] {spnServiceClass, spnHost, spnPort, isStripPort() });
        return new WsmanSPNegoScheme(isStripPort(), spnServiceClass, spnHost, spnPort, useCanonicalHostname);
    }

    private Logger logger = LoggerFactory.getLogger(WsmanSPNegoSchemeFactory.class);

}
