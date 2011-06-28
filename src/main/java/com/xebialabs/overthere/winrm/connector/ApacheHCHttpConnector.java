/*
 * This file is part of WinRM.
 *
 * WinRM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WinRM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WinRM.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere.winrm.connector;

import com.google.common.collect.Lists;
import com.xebialabs.overthere.winrm.HttpConnector;
import com.xebialabs.overthere.winrm.SoapAction;
import com.xebialabs.overthere.winrm.exception.WinRMRuntimeIOException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.NegotiateSchemeFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 */
public class ApacheHCHttpConnector implements HttpConnector {

	private final HttpClient httpclient;
	private final URL targetURL;

	public ApacheHCHttpConnector(String host, int port, String username, String password) {
		httpclient = initialize(host, port, username, password);
		targetURL = getURL(host, port);
	}

	private URL getURL(String host, int port) {
		try {
			//Only http is supported....
			return new URL("http", host, port, "/wsman");
		} catch (MalformedURLException e) {
			throw new WinRMRuntimeIOException("Cannot build a new URL using host " + host + " and port " + port, e);
		}
	}

	private HttpClient initialize(String host, int port, String username, String password) {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		final Credentials credentials;
		if (username.contains("\\")) {
			final String[] split = username.split("\\\\");
			String domain = split[0];
			String simpleUser = split[1];
			//credentials = new NTCredentials(simpleUser, password, null, domain);
			credentials = new UsernamePasswordCredentials("Administrator@win-4yk1f6r5qps", password);

		} else {
			credentials = new UsernamePasswordCredentials(username, password);
		}


		httpclient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);

		NegotiateSchemeFactory nsf = new NegotiateSchemeFactory();
		httpclient.getAuthSchemes().register(AuthPolicy.SPNEGO, nsf);


		List<String> authpref = Lists.newArrayList();
		//authpref.add(AuthPolicy.BASIC);
		//authpref.add(AuthPolicy.NTLM);
		authpref.add(AuthPolicy.SPNEGO);
		//authpref.add(AuthPolicy.);
		//authpref.add(AuthPolicy.SPNEGO);
		final HttpParams params = httpclient.getParams();

		params.setParameter(AuthPNames.TARGET_AUTH_PREF, authpref);
		return httpclient;
	}

	@Override
	public Document sendMessage(Document requestDocument, SoapAction soapAction) {


		Document responseDocument = null;

		try {
			final String requestDocAsString = toString(requestDocument);
			logger.debug("send message:request {}", requestDocAsString);

			StringEntity entity = new StringEntity(requestDocAsString);
			HttpPost httppost = new HttpPost(targetURL.toString());
			if (soapAction != null)
				httppost.addHeader("SOAPAction", soapAction.getValue());
			httppost.addHeader("Content-Type", "application/soap+xml; charset=UTF-8");
			httppost.setEntity(entity);

			final HttpResponse response = httpclient.execute(httppost);
			final StatusLine statusLine = response.getStatusLine();
			logger.debug("status {}", statusLine);
			if (!(statusLine.getStatusCode() == 200)) {
				throw new WinRMRuntimeIOException("Invalid status code " + statusLine + ", " + response);
			}

			HttpEntity entityR = response.getEntity();
			if (entityR == null) {
				throw new WinRMRuntimeIOException("No message associate to the response");
			}

			InputStream is = entityR.getContent();

			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				int n;
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}


			final String text = writer.toString();
			logger.debug("send message:response {}", text);

			responseDocument = DocumentHelper.parseText(text);
			return responseDocument;

		} catch (Exception e) {
			throw new WinRMRuntimeIOException("send message on " + targetURL + " error ", requestDocument, responseDocument, e);
		}
	}

	@Override
	public URL getTargetURL() {
		return targetURL;
	}

	private String toString(Document doc) {
		StringWriter stringWriter = new StringWriter();
		XMLWriter xmlWriter = new XMLWriter(stringWriter, OutputFormat.createPrettyPrint());
		try {
			xmlWriter.write(doc);
			xmlWriter.close();
		} catch (IOException e) {
			throw new WinRMRuntimeIOException("error ", e);
		}
		return stringWriter.toString();
	}

	private static Logger logger = LoggerFactory.getLogger(ApacheHCHttpConnector.class);

}
