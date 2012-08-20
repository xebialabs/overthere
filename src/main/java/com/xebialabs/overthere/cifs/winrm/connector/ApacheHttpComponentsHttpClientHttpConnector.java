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
package com.xebialabs.overthere.cifs.winrm.connector;

import java.io.*;
import java.net.URL;
import java.security.*;
import java.util.HashMap;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.io.Closeables;

import com.xebialabs.overthere.cifs.WinrmHttpsCertificateTrustStrategy;
import com.xebialabs.overthere.cifs.WinrmHttpsHostnameVerificationStrategy;
import com.xebialabs.overthere.cifs.winrm.HttpConnector;
import com.xebialabs.overthere.cifs.winrm.exception.BlankValueRuntimeException;
import com.xebialabs.overthere.cifs.winrm.exception.InvalidFilePathRuntimeException;
import com.xebialabs.overthere.cifs.winrm.exception.WinRMRuntimeIOException;
import com.xebialabs.overthere.cifs.winrm.soap.SoapAction;

import static org.apache.http.auth.AuthScope.ANY_HOST;
import static org.apache.http.auth.AuthScope.ANY_PORT;
import static org.apache.http.auth.AuthScope.ANY_REALM;
import static org.apache.http.client.params.ClientPNames.HANDLE_AUTHENTICATION;

public class ApacheHttpComponentsHttpClientHttpConnector implements HttpConnector {
    private static Logger logger = LoggerFactory.getLogger(ApacheHttpComponentsHttpClientHttpConnector.class);
    private final String username;
    private final boolean useKerberos;
    private final String password;
    private final URL targetURL;
    private WinrmHttpsCertificateTrustStrategy httpsCertTrustStrategy;
    private WinrmHttpsHostnameVerificationStrategy httpsHostnameVerifyStrategy;
    private boolean debugKerberosAuth;

    public ApacheHttpComponentsHttpClientHttpConnector(final String username, final String password, final URL targetURL) {
        this.username = username;
        this.useKerberos = username.contains("@");
        this.password = password;
        this.targetURL = targetURL;
    }

    /**
     * Override the sendMessage method to use custom authentication over HTTP
     */
    @Override
    public Document sendMessage(final Document requestDocument, final SoapAction soapAction) {
        if (useKerberos) {
            return runPrivileged(new PrivilegedSendMessage(this, requestDocument, soapAction));
        } else {
            return doSendMessage(requestDocument, soapAction);
        }
    }

    /**
     * Perform the JAAS login and run the command within a privileged scope.
     * 
     * @param privilegedSendMessage
     *            the PrivilegedSendMessage
     * 
     * @return The result Document
     */
    private Document runPrivileged(final PrivilegedSendMessage privilegedSendMessage) {
        final CallbackHandler handler = new ProvidedAuthCallback(username, password);
        Document result;
        try {
            final LoginContext lc = new LoginContext("", null, handler, new KerberosJaasConfiguration(debugKerberosAuth));
            lc.login();

            result = Subject.doAs(lc.getSubject(), privilegedSendMessage);
        } catch (LoginException e) {
            throw new WinRMRuntimeIOException("Login failure sending message on " + getTargetURL() + " error: " + e.getMessage(),
                privilegedSendMessage.getRequestDocument(), null,
                e);
        } catch (PrivilegedActionException e) {
            throw new WinRMRuntimeIOException("Failure sending message on " + getTargetURL() + " error: " + e
                .getMessage(),
                privilegedSendMessage.getRequestDocument(), null,
                e.getException());
        }
        return result;
    }

    /**
     * CallbackHandler that uses provided username/password credentials.
     */
    private static class ProvidedAuthCallback implements CallbackHandler {

        private String username;
        private String password;

        ProvidedAuthCallback(final String username, final String password) {
            this.username = username;
            this.password = password;
        }

        public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (final Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    final NameCallback nc = (NameCallback) callback;
                    nc.setName(username);
                } else if (callback instanceof PasswordCallback) {
                    final PasswordCallback pc = (PasswordCallback) callback;
                    pc.setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
                }
            }
        }
    }

    private static class KerberosJaasConfiguration extends Configuration {

        private boolean debug;

        private KerberosJaasConfiguration(boolean debug) {
            this.debug = debug;
        }

        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String s) {
            final HashMap<String, String> options = new HashMap<String, String>();
            options.put("client", "true");
            options.put("useTicketCache", "false");
            options.put("useKeyTab", "false");
            options.put("doNotPrompt", "false");
            if (debug) {
                options.put("debug", "true");
            }
            return new AppConfigurationEntry[] { new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options) };
        }

    }

    /**
     * PrivilegedExceptionAction that wraps the internal sendMessage
     */
    private static class PrivilegedSendMessage implements PrivilegedExceptionAction<Document> {
        ApacheHttpComponentsHttpClientHttpConnector connector;
        private Document requestDocument;
        SoapAction soapAction;

        private PrivilegedSendMessage(final ApacheHttpComponentsHttpClientHttpConnector connector, final Document requestDocument,
            final SoapAction soapAction) {
            this.connector = connector;
            this.requestDocument = requestDocument;
            this.soapAction = soapAction;
        }

        @Override
        public Document run() throws Exception {
            return connector.doSendMessage(requestDocument, soapAction);
        }

        public Document getRequestDocument() {
            return requestDocument;
        }
    }

    /**
     * Internal sendMessage, performs the HTTP request and returns the result document.
     */
    private Document doSendMessage(final Document requestDocument, final SoapAction soapAction) {
        final DefaultHttpClient client = new DefaultHttpClient();
        try {
            configureHttpClient(client);
            final HttpContext context = new BasicHttpContext();
            final HttpPost request = new HttpPost(getTargetURL().toURI());

            if (soapAction != null) {
                request.setHeader("SOAPAction", soapAction.getValue());
            }

            final String requestDocAsString = toString(requestDocument);
            logger.trace("Sending request to {}", getTargetURL());
            logger.trace("Request body: {} {}", getTargetURL(), requestDocAsString);

            final HttpEntity entity = createEntity(requestDocAsString);
            request.setEntity(entity);

            final HttpResponse response = client.execute(request, context);

            if (logger.isTraceEnabled()) {
                for (final Header header : response.getAllHeaders()) {
                    logger.trace("Header {}: {}", header.getName(), header.getValue());
                }
            }

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new WinRMRuntimeIOException(
                    "Response code was " + response.getStatusLine().getStatusCode());
            }
            final String text = handleResponse(response, context);
            EntityUtils.consume(response.getEntity());
            logger.trace("Response body: {}", text);

            return DocumentHelper.parseText(text);
        } catch (BlankValueRuntimeException bvrte) {
            throw bvrte;
        } catch (InvalidFilePathRuntimeException ifprte) {
            throw ifprte;
        } catch (Exception e) {
            throw new WinRMRuntimeIOException("Send message on " + getTargetURL() + " error ", requestDocument, null,
                e);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    /**
     * Configure the httpclient for use in all requests.
     */
    private void configureHttpClient(final DefaultHttpClient httpclient) throws NoSuchAlgorithmException,
        KeyManagementException,
        KeyStoreException,
        UnrecoverableKeyException {

        configureTrust(httpclient);

        configureAuthentication(httpclient, "Basic", new BasicUserPrincipal(username));
        if (useKerberos) {
            configureAuthentication(httpclient, "Kerberos", new KerberosPrincipal(username));
        }
        httpclient.getParams().setBooleanParameter(HANDLE_AUTHENTICATION, true);
    }

    /**
     * Configure auth schemes to use for the HttpClient.
     */
    protected void configureAuthentication(final DefaultHttpClient httpclient, final String scheme, final Principal principal) {
        httpclient.getCredentialsProvider().setCredentials(new AuthScope(ANY_HOST, ANY_PORT, ANY_REALM, scheme), new Credentials() {
            public Principal getUserPrincipal() {
                return principal;
            }

            public String getPassword() {
                return password;
            }
        });
    }

    /**
     * Handle the httpResponse and return the SOAP XML String.
     */
    protected String handleResponse(final HttpResponse response, final HttpContext context) throws IOException {
        final HttpEntity entity = response.getEntity();
        if (null == entity.getContentType() || !entity.getContentType().getValue().startsWith(
            "application/soap+xml")) {
            throw new WinRMRuntimeIOException(
                "Send message on " + getTargetURL() + " error: Unexpected content-type: " + entity
                    .getContentType());
        }

        final InputStream is = entity.getContent();
        final Writer writer = new StringWriter();
        final Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        try {
            int n;
            final char[] buffer = new char[1024];
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            Closeables.closeQuietly(reader);
            Closeables.closeQuietly(is);
        }

        return writer.toString();
    }

    /**
     * Configure certificate trust strategy and hostname verifier strategy for the HttpClient
     */
    private void configureTrust(final DefaultHttpClient httpclient) throws NoSuchAlgorithmException,
        KeyManagementException, KeyStoreException, UnrecoverableKeyException {

        if (!"https".equalsIgnoreCase(getTargetURL().getProtocol())) {
            return;
        }

        final TrustStrategy trustStrategy = httpsCertTrustStrategy.getStrategy();
        final X509HostnameVerifier hostnameVerifier = httpsHostnameVerifyStrategy.getVerifier();
        final SSLSocketFactory socketFactory = new SSLSocketFactory(trustStrategy, hostnameVerifier);
        final Scheme sch = new Scheme("https", 443, socketFactory);
        httpclient.getConnectionManager().getSchemeRegistry().register(sch);
    }

    protected static String toString(Document doc) {
        StringWriter stringWriter = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(stringWriter, OutputFormat.createPrettyPrint());
        try {
            xmlWriter.write(doc);
            xmlWriter.close();
        } catch (IOException e) {
            throw new WinRMRuntimeIOException("Cannnot convert XML to String ", e);
        }
        return stringWriter.toString();
    }

    /**
     * Create the HttpEntity to send in the request.
     */
    protected HttpEntity createEntity(final String requestDocAsString) throws UnsupportedEncodingException {
        return new StringEntity(requestDocAsString, ContentType.create("application/soap+xml", "UTF-8"));
    }
    
    public URL getTargetURL() {
        return targetURL;
    }

    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }

    public void setHttpsCertTrustStrategy(WinrmHttpsCertificateTrustStrategy httpsCertTrustStrategy) {
        this.httpsCertTrustStrategy = httpsCertTrustStrategy;
    }

    public void setHttpsHostnameVerifyStrategy(WinrmHttpsHostnameVerificationStrategy httpsHostnameVerifyStrategy) {
        this.httpsHostnameVerifyStrategy = httpsHostnameVerifyStrategy;
    }

    public boolean isDebugKerberosAuth() {
        return debugKerberosAuth;
    }

    public void setDebugKerberosAuth(boolean debugKerberosAuth) {
        this.debugKerberosAuth = debugKerberosAuth;
    }

}
