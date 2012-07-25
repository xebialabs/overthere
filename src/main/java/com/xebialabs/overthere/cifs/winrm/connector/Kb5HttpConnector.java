/*
* Kb5HttpConnector.java
* 
* User: Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
* Created: 5/23/12 3:08 PM
* 
*/
package com.xebialabs.overthere.cifs.winrm.connector;

import com.google.common.io.Closeables;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.cifs.CifsConnectionBuilder;
import com.xebialabs.overthere.cifs.winrm.soap.SoapAction;
import com.xebialabs.overthere.cifs.winrm.exception.BlankValueRuntimeException;
import com.xebialabs.overthere.cifs.winrm.exception.InvalidFilePathRuntimeException;
import com.xebialabs.overthere.cifs.winrm.exception.WinRMRuntimeIOException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.HttpResponse;
import org.apache.http.auth.Credentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.KerberosSchemeFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import static com.xebialabs.overthere.ConnectionOptions.*;

/**
 * Kb5HttpConnector enables Kerberos authentication over HTTP(S).
 *
 * @author Greg Schueler <a href="mailto:greg@dtosolutions.com">greg@dtosolutions.com</a>
 */
public class Kb5HttpConnector extends JdkHttpConnector {
	private static Logger logger = LoggerFactory.getLogger(Kb5HttpConnector.class);
	ConnectionOptions options;
	private String httpsCertTrustStrategy;
	private String httpsHostnameVerifyStrategy;
	private String username;
	private String password;
	private boolean debugKerberosAuth;

	public Kb5HttpConnector(final URL targetURL, final ConnectionOptions options) {
		super(targetURL, null);
		this.options = options;
		this.httpsCertTrustStrategy = options.getOptional(CifsConnectionBuilder.HTTPS_CERTIFICATE_TRUST_STRATEGY);
		this.httpsHostnameVerifyStrategy = options.getOptional(CifsConnectionBuilder.HTTPS_HOSTNAME_VERIFY_STRATEGY);
		this.username = options.getOptional(USERNAME);
		this.password = options.getOptional(PASSWORD);
		this.debugKerberosAuth = options.<Boolean>get(CifsConnectionBuilder.DEBUG_KERBEROS_AUTH, false);
	}

	/**
	 * Override the sendMessage method to use custom authentication over HTTP
	 */
	@Override
	public Document sendMessage(final Document requestDocument, final SoapAction soapAction) {
		return runPrivileged(new PrivilegedSendMessage(this, requestDocument, soapAction));
	}

	/**
     * Perform the JAAS login and run the command within a privileged scope.
     *
     * @param privilegedSendMessage the PrivilegedSendMessage
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
            throw new WinRMRuntimeIOException("Login failure sending message on " + getTargetURL() + " error: "+e.getMessage(),
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
                    throw new UnsupportedCallbackException
                        (callback, "Unrecognized Callback");
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
            if(debug) {
                options.put("debug", "true");
            }
            return new AppConfigurationEntry[] {new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options)};
        }

    }

    /**
     * PrivilegedActionException that wraps the internal sendMessage
     */
    private static class PrivilegedSendMessage implements PrivilegedExceptionAction<Document> {
        Kb5HttpConnector connector;
        private Document requestDocument;
        SoapAction soapAction;

        private PrivilegedSendMessage(final Kb5HttpConnector connector, final Document requestDocument,
                                      final SoapAction soapAction) {
            this.connector = connector;
            this.requestDocument = requestDocument;
            this.soapAction = soapAction;
        }

        @Override
        public Document run() throws Exception {
            return connector.int_sendMessage(requestDocument, soapAction);
        }

        public Document getRequestDocument() {
            return requestDocument;
        }
    }

	/**
	 * Internal sendMessage, performs the HTTP request and returns the result document.
	 */
	private Document int_sendMessage(final Document requestDocument, final SoapAction soapAction) {
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

		configureAuthentication(httpclient);
	}

	/**
	 * Configure auth schemes to use for the HttpClient.
	 */
	protected void configureAuthentication(final DefaultHttpClient httpclient) {
		final Credentials use_jaas_creds = new Credentials() {
			public String getPassword() {
				return password;
			}

			public Principal getUserPrincipal() {
				return new KerberosPrincipal(username);
			}
		};

		httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1, null), use_jaas_creds);

		httpclient.getParams().setBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, true);
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
		KeyManagementException,
		KeyStoreException, UnrecoverableKeyException {
		if (!"https".equalsIgnoreCase(getTargetURL().getProtocol())) {
			return;
		}

		final TrustStrategy trustStrategy;
		final X509HostnameVerifier hostnameVerifier;

		if ("all".equals(httpsCertTrustStrategy)) {
			trustStrategy = new TrustStrategy() {
				@Override
				public boolean isTrusted(final X509Certificate[] chain, final String authType) throws
					CertificateException {
					return true;
				}
			};
		} else if ("self-signed".equals(httpsCertTrustStrategy)) {
			trustStrategy = new TrustSelfSignedStrategy();
		} else {
			//"default"
			trustStrategy = null;
		}
		if ("all".equals(httpsHostnameVerifyStrategy)) {
			hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		} else if ("strict".equals(httpsHostnameVerifyStrategy)) {
			hostnameVerifier = SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
		} else {
			//"browser-compatible"
			hostnameVerifier = SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
		}
		if (null != trustStrategy || null != hostnameVerifier) {
			if (logger.isDebugEnabled()) {
				logger.debug("Configuring httpsTrustCertificates strategy: {}", httpsCertTrustStrategy);
				logger.debug("Configuring httpsVerifyHostname strategy: {}", httpsHostnameVerifyStrategy);
			}
			final SSLSocketFactory socketFactory = new SSLSocketFactory(trustStrategy, hostnameVerifier);
			final Scheme sch = new Scheme("https", 443, socketFactory);
			httpclient.getConnectionManager().getSchemeRegistry().register(sch);
		}
	}

	/**
	 * Create the HttpEntity to send in the request.
	 */
	protected HttpEntity createEntity(final String requestDocAsString) throws UnsupportedEncodingException {
		return new StringEntity(requestDocAsString, ContentType.create("application/soap+xml", "UTF-8"));
	}
}
