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

import com.xebialabs.overthere.cifs.WinrmHttpsCertificateTrustStrategy;
import com.xebialabs.overthere.cifs.WinrmHttpsHostnameVerificationStrategy;
import com.xebialabs.overthere.winrm.soap.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.BasicUserPrincipal;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.*;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static org.apache.http.auth.AuthScope.*;
import static org.apache.http.client.params.AuthPolicy.*;
import static org.apache.http.util.EntityUtils.consume;

/**
 * See http://msdn.microsoft.com/en-us/library/cc251731(v=prot.10).aspx for some examples of how the WS-MAN protocol works on Windows
 */
class WinRmClient {
    private final SocketFactory socketFactory;
    private final String username;
    private final boolean enableKerberos;
    private final String password;
    private final URL targetURL;
    private final String unmappedAddress;
    private final int unmappedPort;

    private String winRmTimeout;
    private int winRmEnvelopSize;
    private String winRmLocale;
    private WinrmHttpsCertificateTrustStrategy httpsCertTrustStrategy;
    private WinrmHttpsHostnameVerificationStrategy httpsHostnameVerifyStrategy;
    private boolean kerberosUseHttpSpn;
    private boolean kerberosAddPortToSpn;
    private boolean kerberosDebug;
    private boolean kerberosTicketCache;
    private int soTimeout;
    private int connectionTimeout;
    private boolean useCanonicalHostname;

    private String shellId;
    private String commandId;
    private int exitValue = -1;
    private int chunk = 0;

    public WinRmClient(final String username, final String password, final URL targetURL, final String unmappedAddress, final int unmappedPort, final SocketFactory socketFactory) {
        int posOfAtSign = username.indexOf('@');
        if (posOfAtSign >= 0) {
            String u = username.substring(0, posOfAtSign);
            String d = username.substring(posOfAtSign + 1);
            if (d.toUpperCase().equals(d)) {
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
        this.unmappedAddress = unmappedAddress;
        this.unmappedPort = unmappedPort;
        this.socketFactory = socketFactory;
    }

    public String createShell() {
        logger.debug("Sending WinRM Create Shell request");

        final Element bodyContent = DocumentHelper.createElement(QName.get("Shell", Namespaces.NS_WIN_SHELL));
        bodyContent.addElement(QName.get("InputStreams", Namespaces.NS_WIN_SHELL)).addText("stdin");
        bodyContent.addElement(QName.get("OutputStreams", Namespaces.NS_WIN_SHELL)).addText("stdout stderr");
        final Document requestDocument = getRequestDocument(Action.WS_ACTION, ResourceURI.RESOURCE_URI_CMD, OptionSet.OPEN_SHELL, bodyContent);

        Document responseDocument = sendRequest(requestDocument, SoapAction.SHELL);

        shellId = getFirstElement(responseDocument, ResponseExtractor.SHELL_ID);

        logger.debug("Received WinRM Create Shell response: shell with ID {} start created", shellId);

        return shellId;
    }

    public String executeCommand(String command) {
        logger.debug("Sending WinRM Execute Command request to shell {}", shellId);

        final Element bodyContent = DocumentHelper.createElement(QName.get("CommandLine", Namespaces.NS_WIN_SHELL));
        String encoded = "\"" + command + "\"";
        bodyContent.addElement(QName.get("Command", Namespaces.NS_WIN_SHELL)).addText(encoded);
        final Document requestDocument = getRequestDocument(Action.WS_COMMAND, ResourceURI.RESOURCE_URI_CMD, OptionSet.RUN_COMMAND, bodyContent);

        Document responseDocument = sendRequest(requestDocument, SoapAction.COMMAND_LINE);

        commandId = getFirstElement(responseDocument, ResponseExtractor.COMMAND_ID);

        logger.debug("Received WinRM Execute Command response to shell {}: command with ID {} was started", shellId, commandId);

        return commandId;
    }

    public boolean receiveOutput(OutputStream stdout, OutputStream stderr) throws IOException {
        logger.debug("Sending WinRM Receive Output request for command {} in shell {}", commandId, shellId);

        final Element bodyContent = DocumentHelper.createElement(QName.get("Receive", Namespaces.NS_WIN_SHELL));
        bodyContent.addElement(QName.get("DesiredStream", Namespaces.NS_WIN_SHELL)).addAttribute("CommandId", commandId).addText("stdout stderr");
        final Document requestDocument = getRequestDocument(Action.WS_RECEIVE, ResourceURI.RESOURCE_URI_CMD, null, bodyContent);

        Document responseDocument = sendRequest(requestDocument, SoapAction.RECEIVE);

        logger.debug("Received WinRM Receive Output response for command {} in shell {}", commandId, shellId);

        handleStream(responseDocument, ResponseExtractor.STDOUT, stdout);
        handleStream(responseDocument, ResponseExtractor.STDERR, stderr);

        if (chunk == 0) {
            parseExitCode(responseDocument);
        }
        chunk++;

        /*
         * We may need to get additional output if the stream has not finished. The CommandState will change from
         * Running to Done like so:
         *
         * @example
         *
         * from... <rsp:CommandState CommandId="..."
         * State="http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Running"/> to...
         * <rsp:CommandState CommandId="..."
         * State="http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Done">
         * <rsp:ExitCode>0</rsp:ExitCode> </rsp:CommandState>
         */
        final List<?> list = ResponseExtractor.STREAM_DONE.getXPath().selectNodes(responseDocument);
        if (!list.isEmpty()) {
            logger.trace("Found CommandState element with State=Done, parsing exit code and returning false.");
            parseExitCode(responseDocument);
            return false;
        } else {
            logger.trace("Did not find CommandState element with State=Done, returning true.");
            return true;
        }
    }

    public void sendInput(byte[] buf) throws IOException {
        logger.debug("Sending WinRM Send Input request for command {} in shell {}", commandId, shellId);

        final Element bodyContent = DocumentHelper.createElement(QName.get("Send", Namespaces.NS_WIN_SHELL));
        final Base64 base64 = new Base64();
        bodyContent.addElement(QName.get("Stream", Namespaces.NS_WIN_SHELL)).addAttribute("Name", "stdin").addAttribute("CommandId", commandId).addText(base64.encodeAsString(buf));
        final Document requestDocument = getRequestDocument(Action.WS_SEND, ResourceURI.RESOURCE_URI_CMD, null, bodyContent);
        sendRequest(requestDocument, SoapAction.SEND);

        logger.debug("Sent WinRM Send Input request for command {} in shell {}", commandId, shellId);
    }

    public void signal() {
        if (commandId == null) {
            logger.warn("Not sending WinRM Signal request in shell {} because there is no running command", shellId);
            return;
        }

        logger.debug("Sending WinRM Signal request for command {} in shell {}", commandId, shellId);

        final Element bodyContent = DocumentHelper.createElement(QName.get("Signal", Namespaces.NS_WIN_SHELL)).addAttribute("CommandId", commandId);
        bodyContent.addElement(QName.get("Code", Namespaces.NS_WIN_SHELL)).addText("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/signal/terminate");
        final Document requestDocument = getRequestDocument(Action.WS_SIGNAL, ResourceURI.RESOURCE_URI_CMD, null, bodyContent);
        sendRequest(requestDocument, SoapAction.SIGNAL);

        logger.debug("Sent WinRM Signal request for command {} in shell {}", commandId, shellId);
    }

    public void deleteShell() {
        if (shellId == null) {
            logger.warn("Not sending WinRM Delete Shell request because there is no shell");
            return;
        }

        logger.debug("Sending WinRM Delete Shell request for shell {}", shellId);

        final Document requestDocument = getRequestDocument(Action.WS_DELETE, ResourceURI.RESOURCE_URI_CMD, null, null);
        sendRequest(requestDocument, null);

        logger.debug("Sent WinRM Delete Shell request for shell {}", shellId);
    }

    public int exitValue() {
        return exitValue;
    }

    private void parseExitCode(Document responseDocument) {
        try {
            logger.trace("Parsing exit code");
            String exitCode = getFirstElement(responseDocument, ResponseExtractor.EXIT_CODE);
            logger.trace("Found exit code {}", exitCode);
            try {
                exitValue = Integer.parseInt(exitCode);
            } catch (NumberFormatException exc) {
                logger.error("Cannot parse exit code {}, setting it to -1", exc);
                exitValue = -1;
            }
        } catch (Exception exc) {
            logger.trace("Exit code not found,");
        }
    }

    private static void handleStream(Document responseDocument, ResponseExtractor stream, OutputStream out) throws IOException {
        @SuppressWarnings("unchecked") final List<Element> streams = stream.getXPath().selectNodes(responseDocument);
        if (!streams.isEmpty()) {
            final Base64 base64 = new Base64();
            Iterator<Element> itStreams = streams.iterator();
            while (itStreams.hasNext()) {
                Element e = itStreams.next();
                // TODO check performance with http://www.iharder.net/current/java/base64/
                final byte[] decode = base64.decode(e.getText());
                out.write(decode);
            }
        }

    }

    private static String getFirstElement(Document doc, ResponseExtractor extractor) {
        @SuppressWarnings("unchecked") final List<Element> nodes = extractor.getXPath().selectNodes(doc);
        if (nodes.isEmpty())
            throw new RuntimeException("Cannot find " + extractor.getXPath() + " in " + toString(doc));

        final Element next = nodes.iterator().next();
        return next.getText();
    }

    private Document getRequestDocument(Action action, ResourceURI resourceURI, OptionSet optionSet, Element bodyContent) {
        SoapMessageBuilder message = Soapy.newMessage();
        SoapMessageBuilder.EnvelopeBuilder envelope = message.envelope();
        try {
            addHeaders(envelope, action, resourceURI, optionSet);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        BodyBuilder body = envelope.body();
        if (bodyContent != null)
            body.setContent(bodyContent);

        return message.getDocument();
    }

    private void addHeaders(SoapMessageBuilder.EnvelopeBuilder envelope, Action action, ResourceURI resourceURI, OptionSet optionSet)
            throws URISyntaxException {
        HeaderBuilder header = envelope.header();
        header.to(targetURL.toURI()).replyTo(new URI("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"));
        header.maxEnvelopeSize(winRmEnvelopSize);
        header.withId(getUUID());
        header.withLocale(winRmLocale);
        header.withTimeout(winRmTimeout);
        header.withAction(action.getUri());
        if (shellId != null) {
            header.withShellId(shellId);
        }
        header.withResourceURI(resourceURI.getUri());
        if (optionSet != null) {
            header.withOptionSet(optionSet.getKeyValuePairs());
        }
    }

    private static String getUUID() {
        return "uuid:" + UUID.randomUUID().toString().toUpperCase();
    }

    private Document sendRequest(final Document requestDocument, final SoapAction soapAction) {
        if (enableKerberos) {
            return runPrivileged(new PrivilegedSendMessage(requestDocument, soapAction));
        } else {
            return doSendRequest(requestDocument, soapAction);
        }
    }

    /**
     * Performs the JAAS login and run the sendRequest method within a privileged scope.
     */
    private Document runPrivileged(final PrivilegedSendMessage privilegedSendMessage) {
        final CallbackHandler handler = new ProvidedAuthCallback(username, password);
        Document result;
        try {
            final LoginContext lc = new LoginContext("", null, handler, new KerberosJaasConfiguration(kerberosDebug, kerberosTicketCache));
            lc.login();

            result = Subject.doAs(lc.getSubject(), privilegedSendMessage);
        } catch (LoginException e) {
            throw new WinRmRuntimeIOException("Login failure sending message on " + targetURL + " error: " + e.getMessage(),
                    privilegedSendMessage.getRequestDocument(), null, e);
        } catch (PrivilegedActionException e) {
            throw new WinRmRuntimeIOException("Failure sending message on " + targetURL + " error: " + e.getMessage(),
                    privilegedSendMessage.getRequestDocument(), null, e.getException());
        }
        return result;
    }

    /**
     * PrivilegedExceptionAction that wraps the internal sendRequest
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
            return WinRmClient.this.doSendRequest(requestDocument, soapAction);
        }

        public Document getRequestDocument() {
            return requestDocument;
        }
    }

    /**
     * Internal sendRequest, performs the HTTP request and returns the result document.
     */
    private Document doSendRequest(final Document requestDocument, final SoapAction soapAction) {
        final HttpClientBuilder client = HttpClientBuilder.create();
        HttpClientConnectionManager connectionManager = getHttpClientConnectionManager();
        try {
            configureHttpClient(client);
            try(CloseableHttpClient httpClient = client.build()) {
                final HttpContext context = new BasicHttpContext();
                final HttpPost request = new HttpPost(targetURL.toURI());

                if (soapAction != null) {
                    request.setHeader("SOAPAction", soapAction.getValue());
                }

                final String requestBody = toString(requestDocument);
                logger.trace("Request:\nPOST {}\n{}", targetURL, requestBody);

                final HttpEntity entity = createEntity(requestBody);
                request.setEntity(entity);

                final HttpResponse response = httpClient.execute(request, context);

                logResponseHeaders(response);

                Document responseDocument = null;
                try {
                    final String responseBody = handleResponse(response, context);
                    responseDocument = DocumentHelper.parseText(responseBody);
                    logDocument("Response body:", responseDocument);
                } catch(WinRmRuntimeIOException e) {
                    if (response.getStatusLine().getStatusCode() == 200) {
                    	throw e;
                    }
                }
                
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new WinRmRuntimeIOException(String.format("Unexpected HTTP response on %s:  %s (%s)",
                            targetURL, response.getStatusLine().getReasonPhrase(), response.getStatusLine().getStatusCode()));
                }

                return responseDocument;
            } finally {
                connectionManager.shutdown();
            }
        } catch (WinRmRuntimeIOException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new WinRmRuntimeIOException("Error when sending request to " + targetURL, requestDocument, null, exc);
        }
    }

    private HttpClientConnectionManager getHttpClientConnectionManager() {
        final Lookup<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", new PlainConnectionSocketFactory() {
            @Override
            public Socket createSocket(HttpContext context) throws IOException {
                return socketFactory.createSocket();
            }
        }).register("https", new SSLConnectionSocketFactory(SSLContexts.createDefault(), SSLConnectionSocketFactory.getDefaultHostnameVerifier()) {
            @Override
            public Socket createSocket(HttpContext context) throws IOException {
                return socketFactory.createSocket();
            }
        }).build();
        return new BasicHttpClientConnectionManager(socketFactoryRegistry);
    }

    private void configureHttpClient(final HttpClientBuilder httpclient) throws GeneralSecurityException {
        configureTrust(httpclient);
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        httpclient.setDefaultCredentialsProvider(credentialsProvider);

        configureAuthentication(credentialsProvider, BASIC, new BasicUserPrincipal(username));

        if (enableKerberos) {
            String spnServiceClass = kerberosUseHttpSpn ? "HTTP" : "WSMAN";
            RegistryBuilder<AuthSchemeProvider> authSchemeRegistryBuilder = RegistryBuilder.create();
            authSchemeRegistryBuilder.register(KERBEROS, new WsmanKerberosSchemeFactory(!kerberosAddPortToSpn, spnServiceClass, unmappedAddress, unmappedPort, useCanonicalHostname));
            authSchemeRegistryBuilder.register(SPNEGO, new WsmanSPNegoSchemeFactory(!kerberosAddPortToSpn, spnServiceClass, unmappedAddress, unmappedPort, useCanonicalHostname));
            httpclient.setDefaultAuthSchemeRegistry(authSchemeRegistryBuilder.build());
            configureAuthentication(credentialsProvider, KERBEROS, new KerberosPrincipal(username));
            configureAuthentication(credentialsProvider, SPNEGO, new KerberosPrincipal(username));
        }

        httpclient.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(soTimeout).build());
        httpclient.setDefaultRequestConfig(RequestConfig.custom().setAuthenticationEnabled(true).setConnectTimeout(connectionTimeout).build());
    }

    private void configureTrust(final HttpClientBuilder httpclientBuilder) throws NoSuchAlgorithmException,
            KeyManagementException, KeyStoreException, UnrecoverableKeyException {

        if (!"https".equalsIgnoreCase(targetURL.getProtocol())) {
            return;
        }

        final TrustStrategy trustStrategy = httpsCertTrustStrategy.getStrategy();
        SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(trustStrategy).build();
        final HostnameVerifier hostnameVerifier = httpsHostnameVerifyStrategy.getVerifier();
        final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        httpclientBuilder.setSSLSocketFactory(socketFactory);
    }

    private void configureAuthentication(CredentialsProvider provider, final String scheme, final Principal principal) {
        provider.setCredentials(new AuthScope(ANY_HOST, ANY_PORT, ANY_REALM, scheme), new Credentials() {
            public Principal getUserPrincipal() {
                return principal;
            }

            public String getPassword() {
                return password;
            }
        });
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
     * Handle the httpResponse and return the SOAP XML String.
     */
    protected String handleResponse(final HttpResponse response, final HttpContext context) throws IOException {
        final HttpEntity entity = response.getEntity();
        if (null == entity.getContentType() || !entity.getContentType().getValue().startsWith("application/soap+xml")) {
            throw new WinRmRuntimeIOException("Error when sending request to " + targetURL + "; Unexpected content-type: " + entity.getContentType());
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
            closeQuietly(reader);
            closeQuietly(is);
            consume(response.getEntity());
        }

        return writer.toString();
    }

    private static String toString(Document doc) {
        StringWriter stringWriter = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(stringWriter, OutputFormat.createPrettyPrint());
        try {
            xmlWriter.write(doc);
            xmlWriter.close();
        } catch (IOException exc) {
            throw new WinRmRuntimeIOException("Cannnot convert XML to String ", exc);
        }
        return stringWriter.toString();
    }

    /**
     * Create the HttpEntity to send in the request.
     */
    protected HttpEntity createEntity(final String requestDocAsString) {
        return new StringEntity(requestDocAsString, ContentType.create("application/soap+xml", "UTF-8"));
    }

    public void setWinRmTimeout(String timeout) {
        this.winRmTimeout = timeout;
    }

    public void setWinRmEnvelopSize(int envelopSize) {
        this.winRmEnvelopSize = envelopSize;
    }

    public void setWinRmLocale(String locale) {
        this.winRmLocale = locale;
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

    public void setKerberosTicketCache(boolean kerberosTicketCache) {
        this.kerberosTicketCache = kerberosTicketCache;
    }

    public int getConnectionTimeout ()
    {
        return connectionTimeout;
    }

    public void setConnectionTimeout ( int connectionTimeout )
    {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout ()
    {
        return soTimeout;
    }

    public void setSoTimeout ( int soTimeout )
    {
        this.soTimeout = soTimeout;
    }


    public boolean isUseCanonicalHostname() {
        return useCanonicalHostname;
    }

    public void setUseCanonicalHostname(boolean useCanonicalHostname) {
        this.useCanonicalHostname = useCanonicalHostname;
    }

    private static Logger logger = LoggerFactory.getLogger(WinRmClient.class);

}
