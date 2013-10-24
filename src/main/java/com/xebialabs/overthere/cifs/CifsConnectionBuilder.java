/**
 * Copyright (c) 2008, 2012, XebiaLabs B.V., All rights reserved.
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
package com.xebialabs.overthere.cifs;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.cifs.telnet.CifsTelnetConnection;
import com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection;
import com.xebialabs.overthere.cifs.winrs.CifsWinrsConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.cifs.WinrmHttpsCertificateTrustStrategy.STRICT;
import static com.xebialabs.overthere.cifs.WinrmHttpsHostnameVerificationStrategy.BROWSER_COMPATIBLE;
import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;

/**
 * Builds CIFS connections.
 */
@Protocol(name = CIFS_PROTOCOL)
public class CifsConnectionBuilder implements OverthereConnectionBuilder {

    /**
     * Name of the protocol handled by this connection builder, i.e. "cifs".
     */
    public static final String CIFS_PROTOCOL = "cifs";

    /**
     * Connection option (<code>CifsConnectionType</code>) that specifies the {@link CifsConnectionType CIFS connection
     * type} to use.
     */
    public static final String CONNECTION_TYPE = "connectionType";

    /**
     * Default port (<code>23</code>) used when the {@link #CONNECTION_TYPE CIFS connection type} is {#link
     * {@link CifsConnectionType#TELNET TELNET}.
     */
    public static final int DEFAULT_TELNET_PORT = 23;

    /**
     * Default port (<code>5985</code>) used when the {@link #WINRM_ENABLE_HTTPS} is set to <tt>false</tt>.
     */
    public static final int DEFAULT_WINRM_HTTP_PORT = 5985;

    /**
     * Default port (<code>5985</code>) used when the {@link #WINRM_ENABLE_HTTPS} is set to <tt>true</tt>.
     */
    public static final int DEFAULT_WINRM_HTTPS_PORT = 5986;

    /**
     * Connection option (Integer) that specifies the CIFS port to connect to.
     */
    public static final String CIFS_PORT = "cifsPort";

    /**
     * Default value (<code>445</code>) for the {@link ConnectionOptions connection option} used to specify the CIFS
     * port to connect to.
     */
    public static final int DEFAULT_CIFS_PORT = 445;

    /**
     * Connection option (<code>Map&lt;String, String&gt;</code>) that specifies the path to share mappings to use for
     * CIFS, specified as a <tt>Map&lt;String, String&gt;</tt>, e.g. "C:\IBM\WebSphere" -> "WebSphere". If a path is not
     * explicitly mapped to a share the administrative share will be used..
     */
    public static final String PATH_SHARE_MAPPINGS = "pathShareMappings";

    /**
     * Default value (empty map) for the {@link ConnectionOptions connection option} used to specify the path to share
     * mappings to use for CIFS.
     */
    public static final Map<String, String> PATH_SHARE_MAPPINGS_DEFAULT = ImmutableMap.of();

    /**
     * Connection option (Boolean) that specifies whether to use HTTPS for WinRM.
     */
    public static final String WINRM_ENABLE_HTTPS = "winrmEnableHttps";

    /**
     * Default value (<code>false</code>) for the connection option used to specify whether to use HTTPS for WinRM.
     */
    public static final boolean DEFAULT_WINRM_ENABLE_HTTPS = false;

    /**
     * Connection option (String) that specifies the context (URI) used by WinRM.
     */
    public static final String WINRM_CONTEXT = "winrmContext";

    /**
     * Default value (<code>/wsman</code>) for the connection option used to specify the context (URI) used by WinRM.
     */
    public static final String DEFAULT_WINRM_CONTEXT = "/wsman";

    /**
     * Connection option (Integer) that specifies the WinRM envelop size in bytes to use.
     */
    public static final String WINRM_ENVELOP_SIZE = "winrmEnvelopSize";

    /**
     * Default value (<code>153600</code>) for the connection option used to specify the WinRM envelop size in bytes to
     * use.
     */
    public static final int DEFAULT_WINRM_ENVELOP_SIZE = 153600;

    /**
     * Connection option (<code>WinrmHttpsCertificateTrustStrategy</code>) that specifies the
     * {@link WinrmHttpsCertificateTrustStrategy} for WinRM HTTPS connections.
     */
    public static final String WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY = "winrmHttpsCertificateTrustStrategy";

    /**
     * Default value ({@link WinrmHttpsCertificateTrustStrategy#STRICT}) of the {@link ConnectionOptions connection
     * option} used to specify the {@link WinrmHttpsCertificateTrustStrategy} for WinRM HTTPS connections.
     */
    public static final WinrmHttpsCertificateTrustStrategy DEFAULT_WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY = STRICT;

    /**
     * Connection option (<code>WinrmHttpsHostnameVerificationStrategy</code>) that specifies the
     * {@link WinrmHttpsHostnameVerificationStrategy} for WinRM HTTPS connections.
     */
    public static final String WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY = "winrmHttpsHostnameVerificationStrategy";

    /**
     * Default value ({@link WinrmHttpsHostnameVerificationStrategy#BROWSER_COMPATIBLE}) of the
     * {@link ConnectionOptions connection option} used to specify the {@link WinrmHttpsHostnameVerificationStrategy}
     * for WinRM HTTPS connections.
     */
    public static final WinrmHttpsHostnameVerificationStrategy DEFAULT_WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY = BROWSER_COMPATIBLE;

    /**
     * Connection option (Boolean) that specifies whether to enable debug output for Kerberos authentication.
     */
    public static final String WINRM_KERBEROS_DEBUG = "winrmKerberosDebug";

    /**
     * Default value (<code>false</code>) for the connection option used to specify whether to enable debug output for
     * Kerberos authentication.
     */
    public static final boolean DEFAULT_WINRM_KERBEROS_DEBUG = false;

    /**
     * Connection option (Boolean) that specifies whether to use the <code>HTTP</code> protocol in the SPN for Kerberos
     * authentication.
     */
    public static final String WINRM_KERBEROS_USE_HTTP_SPN = "winrmKerberosUseHttpSpn";

    /**
     * Default value (<code>false</code>) for the connection option used to specify whether to use the <code>HTTP</code>
     * protocol in the SPN for Kerberos authentication.
     */
    public static final boolean DEFAULT_WINRM_KERBEROS_USE_HTTP_SPN = false;

    /**
     * Connection option (Boolean) that specifies whether to add the port to the SPN for Kerberos authentication.
     */
    public static final String WINRM_KERBEROS_ADD_PORT_TO_SPN = "winrmKerberosAddPortToSpn";

    /**
     * Default value (<code>false</code>) for the connection option used to specify whether to add the port to the SPN
     * for Kerberos authentication.
     */
    public static final boolean DEFAULT_WINRM_KERBEROS_ADD_PORT_TO_SPN = false;

    /**
     * Connection option (String) that specifies the WinRM locale to use.
     */
    public static final String WINRM_LOCALE = "winrmLocale";

    /**
     * Default value (<code>en-US</code>) for the connection option used to specify the WinRM locale to use.
     */
    public static final String DEFAULT_WINRM_LOCALE = "en-US";

    /**
     * Connection option (String) that specifies the WinRM timeout in <a
     * href="http://www.w3.org/TR/xmlschema-2/#isoformats">XML schema duration format</a>
     */
    public static final String WINRM_TIMEMOUT = "winrmTimeout";

    /**
     * Default value (<code>PT60.000S</code>) for the connection option used to specify the WinRM timeout.
     */
    public static final String DEFAULT_WINRM_TIMEOUT = "PT60.000S";

    /**
     * Connection option (Boolean) that specifies whether to allow the user's credentials to be passed to the remote
     * host. Corresponds to the <code>winrs</code> command option <code>-noprofile</code>.
     */
    public static final String WINRS_ALLOW_DELEGATE = "winrsAllowDelegate";

    /**
     * Default value (<code>false</code>) for the connection option used to specify whether to allow the user's
     * credentials to be passed to the remote machine
     */
    public static final boolean DEFAULT_WINRS_ALLOW_DELEGATE = false;

    /**
     * Connection option (Boolean) that specifies whether to enable compression. Corresponds to the <code>winrs</code>
     * command option <code>-compression</code>.
     */
    public static final String WINRS_COMPRESSION = "winrsCompression";

    /**
     * Default value (<code>false</code>) for the connection option used to specify whether to enable compression.
     */
    public static final boolean DEFAULT_WINRS_COMPRESSION = false;

    /**
     * Connection option (Boolean) that specifies whether to disable echo. Corresponds to the <code>winrs</code> command
     * option <code>-noecho</code>.
     */
    public static final String WINRS_NOECHO = "winrsNoecho";

    /**
     * Default value (<code>false</code>) for the connection option used to specify whether to disable echo.
     */
    public static final boolean DEFAULT_WINRS_NOECHO = false;

    /**
     * Connection option (Boolean) that specifies whether to disable loading the user profile before executing the
     * command. Corresponds to the <code>winrs</code> command option <code>-noprofile</code>.
     */
    public static final String WINRS_NOPROFILE = "winrsNoprofile";

    /**
     * Default value (<code>false</code>) for the connection option used to specify whether to disable loading the user
     * profile before executing the command.
     */
    public static final boolean DEFAULT_WINRS_NOPROFILE = false;

    /**
     * Connection option (Boolean) that specifies whether to disable encryption. Corresponds to the <code>winrs</code>
     * command option <code>-unencrypted</code>.
     */
    public static final String WINRS_UNENCRYPTED = "winrsUnencrypted";

    /**
     * Default value (<code>false</code>) for the connection option used to specify whether to disable encryption.
     */
    public static final boolean DEFAULT_WINRS_UNENCRYPTED = false;

    /**
     * Connection option (String) that specifies the protocol for the "winrs proxy".
     */
    public static final String WINRS_PROXY_PROTOCOL = "winrsProxyProtocol";

    /**
     * Default value (<code>local</code>) for the connection option that specifies the protocol for the "winrs proxy".
     */
    public static final String DEFAULT_WINRS_PROXY_PROTOCOL = LOCAL_PROTOCOL;

    /**
     * Connection option (<code>ConnectionOptions</code>) that specifies the {@link ConnectionOptions connection
     * options} to be used for the "winrs proxy".
     */
    public static final String WINRS_PROXY_CONNECTION_OPTIONS = "winrsProxyConnectionOptions";

    private OverthereConnection connection;

    public CifsConnectionBuilder(String type, ConnectionOptions options, AddressPortMapper mapper) {
        CifsConnectionType cifsConnectionType = options.getEnum(CONNECTION_TYPE, CifsConnectionType.class);

        switch (cifsConnectionType) {
        case TELNET:
            connection = new CifsTelnetConnection(type, options, mapper);
            break;
        case WINRM_INTERNAL:
            connection = new CifsWinRmConnection(type, options, mapper);
            break;
        case WINRM_NATIVE:
        	connection = new CifsWinrsConnection(type, options, mapper);
        	break;
        default:
            throw new IllegalArgumentException("Unknown CIFS connection type " + cifsConnectionType);
        }
    }

    @Override
    public OverthereConnection connect() {
        return connection;
    }

    @Override
    public String toString() {
        return connection.toString();
    }

}
