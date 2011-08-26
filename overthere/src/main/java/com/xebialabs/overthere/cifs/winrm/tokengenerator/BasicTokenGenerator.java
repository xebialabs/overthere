package com.xebialabs.overthere.cifs.winrm.tokengenerator;

import org.apache.commons.codec.binary.Base64;

import com.xebialabs.overthere.cifs.winrm.TokenGenerator;

public class BasicTokenGenerator implements TokenGenerator {

	private String username;

	private String password;

	public BasicTokenGenerator(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public String generateToken() {
		String credentials = String.format("%s:%s", username, password);
		return "Basic " + Base64.encodeBase64String(credentials.getBytes());
	}

}
