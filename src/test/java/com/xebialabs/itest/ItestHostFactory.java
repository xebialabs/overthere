package com.xebialabs.itest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItestHostFactory {

	// The field logger needs to be defined up here so that the static initialized below can use the logger
	private static Logger logger = LoggerFactory.getLogger(ItestHostFactory.class);

	private static Properties itestProperties;

	static {
		itestProperties = readItestProperties();
	}

	public static ItestHost getItestHost(String hostId) {
		String amiId = itestProperties.getProperty(hostId + ".amiId");

		if (amiId != null) {
			logger.info("Using Amazon EC2 for integration tests on " + hostId);
			return new Ec2ItestHost(hostId, amiId, itestProperties);
		}

		logger.info("Using existing host for integration tests on " + hostId);
		return new ExistingItestHost(hostId, itestProperties);
	}

	private static Properties readItestProperties() {
		try {
			Properties itestProperties = new Properties();
			File itestPropertiesFile = new File("itest.properties");
			if (itestPropertiesFile.exists()) {
				FileInputStream in = new FileInputStream(itestPropertiesFile);
				try {
					itestProperties.load(in);
				} finally {
					in.close();
				}
			} else {
				logger.warn("itest.properties not found, falling back to using existing hosts");
			}
			return itestProperties;
		} catch (IOException exc) {
			throw new RuntimeException("Cannot read itest.properties", exc);
		}
	}
	
}
