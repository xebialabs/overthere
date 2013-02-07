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

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.kerberos.KerberosPrincipal;
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
import org.apache.http.impl.auth.KerberosSchemeFactory;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
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
import static org.apache.http.client.params.AuthPolicy.BASIC;
import static org.apache.http.client.params.AuthPolicy.KERBEROS;
import static org.apache.http.client.params.AuthPolicy.SPNEGO;
import static org.apache.http.client.params.ClientPNames.HANDLE_AUTHENTICATION;

public class ApacheHttpComponentsHttpClientHttpConnector implements HttpConnector {
    private static Logger logger = LoggerFactory.getLogger(ApacheHttpComponentsHttpClientHttpConnector.class);
    private final String username;
    private final boolean enableKerberos;
    private final String password;
    private final URL targetURL;
    private WinrmHttpsCertificateTrustStrategy httpsCertTrustStrategy;
    private WinrmHttpsHostnameVerificationStrategy httpsHostnameVerifyStrategy;
    private boolean kerberosUseHttpSpn;
    private boolean kerberosAddPortToSpn;
    private boolean kerberosDebug;

    public ApacheHttpComponentsHttpClientHttpConnector(final String username, final String password, final URL targetURL) {
        int posOfAtSign = username.indexOf('@');
        if(posOfAtSign >= 0) {
            String u = username.substring(0, posOfAtSign);
            String d = username.substring(posOfAtSign + 1);
            if(d.toUpperCase().equals(d)) {
                this.username = username;
            } else {
                this.username = u + "@" + d.toUpperCase();
                logger.warn("Fixing username [{}] to have an upper case domain name [{}]", username, this.username);
            }
            this.enableKerberos = true;
        } else {
            this.username = username;
            this.enableKerberos = false;
        }
        this.password = password;
        this.targetURL = targetURL;
    }

    /**
     * Override the sendMessage method to use custom authentication over HTTP
     */
    @Override
    public Document sendMessage(final Document requestDocument, final SoapAction soapAction) {
        if (enableKerberos) {
            return runPrivileged(new PrivilegedSendMessage(requestDocument, soapAction));
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
            final LoginContext lc = new LoginContext("", null, handler, new KerberosJaasConfiguration(kerberosDebug));
            lc.login();

            result = Subject.doAs(lc.getSubject(), privilegedSendMessage);
        } catch (LoginException e) {
            throw new WinRMRuntimeIOException("Login failure sending message on " + getTargetURL() + " error: " + e.getMessage(),
                privilegedSendMessage.getRequestDocument(), null, e);
        } catch (PrivilegedActionException e) {
            throw new WinRMRuntimeIOException("Failure sending message on " + getTargetURL() + " error: " + e.getMessage(),
                privilegedSendMessage.getRequestDocument(), null, e.getException());
        }
        return result;
    }

    /**
     * PrivilegedExceptionAction that wraps the internal sendMessage
     */
    private class PrivilegedSendMessage implements PrivilegedExceptionAction<Document> {
        private Document requestDocument;
        private SoapAction soapAction;

        private PrivilegedSendMessage(final Document requestDocument, final SoapAction soapAction) {
            this.requestDocument = requestDocument;
            this.soapAction = soapAction;
        }

        @Override
        public Document run() throws Exception {
            return ApacheHttpComponentsHttpClientHttpConnector.this.doSendMessage(requestDocument, soapAction);
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

            final String requestBody = toString(requestDocument);
            logger.trace("Request:\nPOST {}\n{}", getTargetURL(), requestBody);

            final HttpEntity entity = createEntity(requestBody);
            request.setEntity(entity);

            final HttpResponse response = client.execute(request, context);

            logResponseHeaders(response);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new WinRMRuntimeIOException(String.format("Unexpected HTTP response on %s:  %s (%s)",
                    getTargetURL(), response.getStatusLine().getReasonPhrase(), response.getStatusLine().getStatusCode()));
            }

            final String responseBody = handleResponse(response, context);
            Document responseDocument = DocumentHelper.parseText(responseBody);

            logDocument("Response body:", responseDocument);

            return responseDocument;
        } catch (BlankValueRuntimeException exc) {
            throw exc;
        } catch (InvalidFilePathRuntimeException exc) {
            throw exc;
        } catch (WinRMRuntimeIOException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new WinRMRuntimeIOException("Error when sending request to " + getTargetURL(), requestDocument, null, exc);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    private static void logResponseHeaders(final HttpResponse response) {
        if (!logger.isTraceEnabled()) {
            return;
        }

        StringBuilder headers = new StringBuilder();
        for (final Header header : response.getAllHeaders()) {
            headers.append(header.getName()).append(": ").append(header.getValue()).append("\n");
        }

        logger.trace("Response headers:\n{}", headers);
    }

    private static void logDocument(String caption, final Document document) {
        if (!logger.isTraceEnabled()) {
            return;
        }

        StringWriter text = new StringWriter();
        try {
            XMLWriter writer = new XMLWriter(text, OutputFormat.createPrettyPrint());
            writer.write(document);
            writer.close();
        } catch (IOException e) {
            logger.trace("{}\n{}", caption, e);
        }

        logger.trace("{}\n{}", caption, text);
    }

    /**
     * Configure the httpclient for use in all requests.
     */
    private void configureHttpClient(final DefaultHttpClient httpclient) throws NoSuchAlgorithmException,
        KeyManagementException,
        KeyStoreException,
        UnrecoverableKeyException {

        configureTrust(httpclient);

        configureAuthentication(httpclient, BASIC, new BasicUserPrincipal(username));

        if (enableKerberos) {
            if (kerberosUseHttpSpn) {
                httpclient.getAuthSchemes().register(KERBEROS, new KerberosSchemeFactory(!kerberosAddPortToSpn));
                httpclient.getAuthSchemes().register(SPNEGO, new SPNegoSchemeFactory(!kerberosAddPortToSpn));
            } else {
                httpclient.getAuthSchemes().register(KERBEROS, new WsmanKerberosSchemeFactory(!kerberosAddPortToSpn));
                httpclient.getAuthSchemes().register(SPNEGO, new WsmanSPNegoSchemeFactory(!kerberosAddPortToSpn));
            }
            configureAuthentication(httpclient, KERBEROS, new KerberosPrincipal(username));
            configureAuthentication(httpclient, SPNEGO, new KerberosPrincipal(username));
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
        if (null == entity.getContentType() || !entity.getContentType().getValue().startsWith("application/soap+xml")) {
            throw new WinRMRuntimeIOException("Error when sending request to " + getTargetURL() + "; Unexpected content-type: " + entity.getContentType());
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
            EntityUtils.consume(response.getEntity());
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
    protected HttpEntity createEntity(final String requestDocAsString) {
        return new StringEntity(requestDocAsString, ContentType.create("application/soap+xml", "UTF-8"));
    }

    public URL getTargetURL() {
        return targetURL;
    }

    public void setHttpsCertTrustStrategy(WinrmHttpsCertificateTrustStrategy httpsCertTrustStrategy) {
        this.httpsCertTrustStrategy = httpsCertTrustStrategy;
    }

    public void setHttpsHostnameVerifyStrategy(WinrmHttpsHostnameVerificationStrategy httpsHostnameVerifyStrategy) {
        this.httpsHostnameVerifyStrategy = httpsHostnameVerifyStrategy;
    }

    public void setKerberosUseHttpSpn(boolean kerberosUseHttpSpn) {
        this.kerberosUseHttpSpn = kerberosUseHttpSpn;
    }

    public void setKerberosAddPortToSpn(boolean kerberosAddPortToSpn) {
        this.kerberosAddPortToSpn = kerberosAddPortToSpn;
    }

    public void setKerberosDebug(boolean kerberosDebug) {
        this.kerberosDebug = kerberosDebug;
    }

}
