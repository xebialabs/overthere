package com.xebialabs.overthere.winrm.tokengenerator;

import com.xebialabs.overthere.winrm.TokenGenerator;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicTokenGenerator implements TokenGenerator {

	private static final Base64 B64_ENCODER = new Base64();

	private final String credential;

	public BasicTokenGenerator(String host, String username, String password) {
		credential = String.format("%s:%s", username, password);
	}

	@Override
	public String generateToken() {
		byte[] token = B64_ENCODER.encode(credential.getBytes());
		String response = "Basic " + new String(token).trim();
		logger.debug("Basic token: "+response);
		return response;
	}

	private final static Logger logger = LoggerFactory.getLogger(BasicTokenGenerator.class);
}
