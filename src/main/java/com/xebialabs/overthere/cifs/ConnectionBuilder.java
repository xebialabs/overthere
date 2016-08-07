package com.xebialabs.overthere.cifs;

import static com.xebialabs.overthere.cifs.WinrmHttpsCertificateTrustStrategy.STRICT;
import static com.xebialabs.overthere.cifs.WinrmHttpsHostnameVerificationStrategy.BROWSER_COMPATIBLE;
import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;

public abstract class ConnectionBuilder {

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_connectionType">the online documentation</a>
     */
    public static final String CONNECTION_TYPE = "connectionType";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_connectionType">the online documentation</a>
     */
    public static final int PORT_DEFAULT_TELNET = 23;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_connectionType">the online documentation</a>
     */
    public static final int PORT_DEFAULT_WINRM_HTTP = 5985;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_connectionType">the online documentation</a>
     */
    public static final int PORT_DEFAULT_WINRM_HTTPS = 5986;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmEnableHttps">the online documentation</a>
     */
    public static final String WINRM_ENABLE_HTTPS = "winrmEnableHttps";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmEnableHttps">the online documentation</a>
     */
    public static final boolean WINRM_ENABLE_HTTPS_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmContext">the online documentation</a>
     */
    public static final String WINRM_CONTEXT = "winrmContext";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmContext">the online documentation</a>
     */
    public static final String WINRM_CONTEXT_DEFAULT = "/wsman";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmEnvelopSize">the online documentation</a>
     */
    public static final String WINRM_ENVELOP_SIZE = "winrmEnvelopSize";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmEnvelopSize">the online documentation</a>
     */
    public static final int WINRM_ENVELOP_SIZE_DEFAULT = 153600;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmHttpsCertificateTrustStrategy">the online documentation</a>
     */
    public static final String WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY = "winrmHttpsCertificateTrustStrategy";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmHttpsCertificateTrustStrategy">the online documentation</a>
     */
    public static final WinrmHttpsCertificateTrustStrategy WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY_DEFAULT = STRICT;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmHttpsHostnameVerificationStrategy">the online documentation</a>
     */
    public static final String WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY = "winrmHttpsHostnameVerificationStrategy";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmHttpsHostnameVerificationStrategy">the online documentation</a>
     */
    public static final WinrmHttpsHostnameVerificationStrategy WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY_DEFAULT = BROWSER_COMPATIBLE;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmKerberosDebug">the online documentation</a>
     */
    public static final String WINRM_KERBEROS_DEBUG = "winrmKerberosDebug";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmKerberosDebug">the online documentation</a>
     */
    public static final boolean WINRM_KERBEROS_DEBUG_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmKerberosTicketCache">the online documentation</a>
     */
    public static final String WINRM_KERBEROS_TICKET_CACHE = "winrmKerberosTicketCache";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmKerberosTicketCache">the online documentation</a>
     */
    public static final boolean WINRM_KERBEROS_TICKET_CACHE_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmLocale">the online documentation</a>
     */
    public static final String WINRM_LOCALE = "";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmLocale">the online documentation</a>
     */
    public static final String WINRM_LOCALE_DEFAULT = "en-US";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmKerberosUseHttpSpn">the online documentation</a>
     */
    public static final String WINRM_KERBEROS_USE_HTTP_SPN = "winrmKerberosUseHttpSpn";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmKerberosUseHttpSpn">the online documentation</a>
     */
    public static final boolean WINRM_KERBEROS_USE_HTTP_SPN_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmKerberosAddPortToSpn">the online documentation</a>
     */
    public static final String WINRM_KERBEROS_ADD_PORT_TO_SPN = "winrmKerberosAddPortToSpn";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmKerberosAddPortToSpn">the online documentation</a>
     */
    public static final boolean WINRM_KERBEROS_ADD_PORT_TO_SPN_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmTimeout">the online documentation</a>
     */
    public static final String WINRM_TIMEMOUT = "winrmTimeout";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrmTimeout">the online documentation</a>
     */
    public static final String DEFAULT_WINRM_TIMEOUT = "PT60.000S";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrsAllowDelegate">the online documentation</a>
     */
    public static final String WINRS_ALLOW_DELEGATE = "winrsAllowDelegate";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrsAllowDelegate">the online documentation</a>
     */
    public static final boolean DEFAULT_WINRS_ALLOW_DELEGATE = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrsCompression">the online documentation</a>
     */
    public static final String WINRS_COMPRESSION = "winrsCompression";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrsCompression">the online documentation</a>
     */
    public static final boolean WINRS_COMPRESSION_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrsNoecho">the online documentation</a>
     */
    public static final String WINRS_NOECHO = "winrsNoecho";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrsNoecho">the online documentation</a>
     */
    public static final boolean WINRS_NOECHO_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrsNoprofile">the online documentation</a>
     */
    public static final String WINRS_NOPROFILE = "winrsNoprofile";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrsNoprofile">the online documentation</a>
     */
    public static final boolean WINRS_NOPROFILE_DEFAULT = false;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrsProxyProtocol">the online documentation</a>
     */
    public static final String WINRS_PROXY_PROTOCOL = "winrsProxyProtocol";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrsProxyProtocol">the online documentation</a>
     */
    public static final String WINRS_PROXY_PROTOCOL_DEFAULT = LOCAL_PROTOCOL;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrsProxyConnectionOptions">the online documentation</a>
     */
    public static final String WINRS_PROXY_CONNECTION_OPTIONS = "winrsProxyConnectionOptions";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrsUnencrypted">the online documentation</a>
     */
    public static final String WINRS_UNENCRYPTED = "winrsUnencrypted";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#cifs_winrsUnencrypted">the online documentation</a>
     */
    public static final boolean WINRS_UNENCRYPTED_DEFAULT = false;
}
