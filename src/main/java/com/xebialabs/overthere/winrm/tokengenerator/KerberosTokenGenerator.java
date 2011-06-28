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
package com.xebialabs.overthere.winrm.tokengenerator;

import com.google.common.base.Strings;
import com.xebialabs.overthere.winrm.TokenGenerator;
import com.xebialabs.overthere.winrm.exception.BlankValueRuntimeException;
import com.xebialabs.overthere.winrm.exception.InvalidFilePathRuntimeException;
import org.ietf.jgss.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.PrivilegedAction;

/**
 *
 */
public class KerberosTokenGenerator implements TokenGenerator {

	private final static String LOGIN_CONTEX_ENTRY = "WinRMClient";

	private Subject subject;
	private GSSContext context;

	private final String host;
	private final String username;
	private final String password;

	private String token;

	public KerberosTokenGenerator(String host, String username, String password) {
		this.host = host;
		this.username = username;
		this.password = password;
	}


	public String generateToken() {

		checkSystemProperty("java.security.auth.login.config");
		checkSystemProperty("java.security.krb5.conf");

		//Generate the token only once...when the subject has not been authenticated yet.
		if (subject == null) {
			try {
				login();
				final byte[] ticket = initiateSecurityContext();
				byte[] encodedBytes = org.apache.commons.codec.binary.Base64.encodeBase64(ticket);
				String encoded = new String(encodedBytes);
				token = "Negotiate " + encoded;
				logger.debug("generateToken {} {} {} {}", new Object[]{host, username, "********", token});
				return token;
			} catch (Exception e) {
				throw new RuntimeException("generation of the token for user " + username + " failed ", e);
			}
		}
		logger.debug("{} has been authenticated, return null", username);
		return null;
	}

	private void checkSystemProperty(String key) {
		final String file = System.getProperty(key);
		if (Strings.isNullOrEmpty(file)) {
			throw new BlankValueRuntimeException("the '" + key + "' has not been set");
		}
		logger.debug("{}:{}", key, file);
		final File configurationFile = new File(file);
		if (!configurationFile.exists())
			throw new InvalidFilePathRuntimeException(key, file);

		if (logger.isDebugEnabled()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(configurationFile));
				String str;
				while ((str = in.readLine()) != null) {
					logger.debug("{} {}", key, str);
				}
				in.close();
			} catch (IOException e) {
			}
		}
	}

	private void login() throws LoginException {
		LoginContext loginCtx = null;
		loginCtx = new LoginContext(LOGIN_CONTEX_ENTRY, new CallbackHandler() {
			@Override
			public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
				for (int i = 0; i < callbacks.length; i++) {
					if (callbacks[i] instanceof NameCallback && username != null) {
						NameCallback nc = (NameCallback) callbacks[i];
						nc.setName(username);
					} else if (callbacks[i] instanceof PasswordCallback) {
						PasswordCallback pc = (PasswordCallback) callbacks[i];
						pc.setPassword(password.toCharArray());
					} else {
						/*throw new UnsupportedCallbackException(
												callbacks[i], "Unrecognized Callback");*/
					}
				}
			}
		});
		loginCtx.login();
		this.subject = loginCtx.getSubject();
	}

	private byte[] initiateSecurityContext()
			throws GSSException {


		byte svc_tkt[] = Subject.doAs(subject, new PrivilegedAction<byte[]>() {
			public byte[] run() {
				try {
					final Oid krb5MechOid = new Oid("1.2.840.113554.1.2.2");

					GSSManager manager = GSSManager.getInstance(); //new GSSManagerImpl(5); //GSSManager.getInstance();
					GSSName serverName = manager.createName("HTTP/" + host, null /*GSSName.NT_USER_NAME*/);
					context = manager.createContext(serverName, krb5MechOid, null /*gssCred*/, GSSContext.DEFAULT_LIFETIME);
					context.requestCredDeleg(true);
					byte[] token = new byte[0];
					token = context.initSecContext(token, 0, token.length);

					/*GSSName gssNamekrb = manager.createName(username, GSSName.NT_USER_NAME, krb5MechOid);
					GSSCredential gssCred = manager.createCredential(gssNamekrb, GSSCredential.DEFAULT_LIFETIME, krb5MechOid, GSSCredential.INITIATE_ONLY);
					Oid[] mechs = gssCred.getMechs();

					//list out themechs in case we wanted to see which ones are used (we already know)
					if (mechs != null)
						for (int i = 0; i < mechs.length; i++)
							System.out.println("Credential Mechanism: " + mechs[i].toString());

					GSSName serverName = manager.createName("HTTP/" + host, GSSName.NT_USER_NAME);
					context = manager.createContext(serverName, krb5MechOid, gssCred, GSSContext.DEFAULT_LIFETIME);


					//set the context flags....we're always setting the delegation flag
					//to true here


					context.requestMutualAuth(false);
					context.requestCredDeleg(true);
					context.requestReplayDet(false);
					context.requestSequenceDet(false);
					context.requestConf(true);
					context.requestInteg(true);
					context.requestAnonymity(false);

					token = context.initSecContext(token, 0, token.length);
                    */

					logger.debug("Remaining lifetime in seconds {} ", context.getLifetime());
					logger.debug("Context mechanism {} ", context.getMech().toString());
					logger.debug("Initiator {}", context.getSrcName().toString());
					logger.debug("Acceptor {}", context.getTargName().toString());

					if (context.getConfState())
						System.out.println("Confidentiality security service available");

					if (context.getIntegState())
						System.out.println("Integrity security service available");

					logger.debug("========== END  ACQUIRE SERVICE TKT =============");

					return token;
				} catch (GSSException gsse) {
					throw new RuntimeException("GSS Error", gsse);
				} catch (Exception e) {
					throw new RuntimeException("Security Error", e);

				}
			}
		});
		return svc_tkt;
	}

	private final static Logger logger = LoggerFactory.getLogger(KerberosTokenGenerator.class);

	static {
		if (logger.isDebugEnabled()) {
			System.setProperty("javax.net.debug", "all");
			System.setProperty("java.security.krb5.debug", "true");
			System.setProperty("java.security.jgss.debug", "true");
			System.setProperty("sun.security.spnego.debug", "true");
			System.setProperty("sun.security.krb5.debug", "true");
			System.setProperty("security.debug.loginContext", "all");
		}
	}
}
