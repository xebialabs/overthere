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
package com.xebialabs.overthere.cifs.winrm.connector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;
import com.xebialabs.overthere.cifs.winrm.HttpConnector;
import com.xebialabs.overthere.cifs.winrm.soap.SoapAction;
import com.xebialabs.overthere.cifs.winrm.TokenGenerator;
import com.xebialabs.overthere.cifs.winrm.exception.BlankValueRuntimeException;
import com.xebialabs.overthere.cifs.winrm.exception.InvalidFilePathRuntimeException;
import com.xebialabs.overthere.cifs.winrm.exception.WinRMRuntimeIOException;

/**
 */
public class JdkHttpConnector implements HttpConnector {

	private final URL targetURL;

	private final TokenGenerator tokenGenerator;

	public JdkHttpConnector(URL targetURL, TokenGenerator tokenGenerator) {
		this.targetURL = targetURL;
		this.tokenGenerator = tokenGenerator;
	}

	@Override
	public Document sendMessage(Document requestDocument, SoapAction soapAction) {
		try {
			final URLConnection urlConnection = targetURL.openConnection();
			HttpURLConnection con = (HttpURLConnection) urlConnection;

			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/soap+xml; charset=UTF-8");

			final String authToken = tokenGenerator.generateToken();
			if (authToken != null)
				con.addRequestProperty("Authorization", authToken);

			if (soapAction != null) {
				con.setRequestProperty("SOAPAction", soapAction.getValue());
			}

			final String requestDocAsString = toString(requestDocument);
			logger.trace("Sending request to {}", targetURL);
			logger.trace("Request body: {} {}", targetURL, requestDocAsString);
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(
							con.getOutputStream()));
            try {
			    bw.write(requestDocAsString, 0, requestDocAsString.length());
            } finally {
                Closeables.closeQuietly(bw);
            }

			InputStream is = null;
			if (con.getResponseCode() >= 400) {
			    is = con.getErrorStream();
			}
			if(is == null) {
			    is = con.getInputStream();
			}
			
			Writer writer = new StringWriter();
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			try {
				int n;
				char[] buffer = new char[1024];
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
                Closeables.closeQuietly(reader);
				Closeables.closeQuietly(is);
			}

			if (logger.isDebugEnabled()) {
				for (int i = 0; i < con.getHeaderFields().size(); i++) {
					logger.trace("Header {}: {}", con.getHeaderFieldKey(i), con.getHeaderField(i));
				}
			}

			final String text = writer.toString();
			logger.trace("Response body: {}", text);

			return DocumentHelper.parseText(text);
		} catch (BlankValueRuntimeException bvrte) {
			throw bvrte;
		} catch (InvalidFilePathRuntimeException ifprte) {
			throw ifprte;
		} catch (Exception e) {
			throw new WinRMRuntimeIOException("Send message on " + targetURL + " error ", requestDocument, null, e);
		}
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

	private static Logger logger = LoggerFactory.getLogger(JdkHttpConnector.class);

	/**
	 * Return the targetURL
	 */
	protected URL getTargetURL() {
		return targetURL;
	}
}
