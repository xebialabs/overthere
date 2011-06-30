package com.xebialabs.itest;

import static com.google.common.base.Preconditions.checkState;

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

	public static ItestHost getItestHostThatDoesNotRequiresTeardown(String hostLabel) {
		return getItestHost(hostLabel, true);
	}

	public static ItestHost getItestHost(String hostLabel) {
		return getItestHost(hostLabel, false);
	}

	private static ItestHost getItestHost(String hostLabel, boolean disableEc2) {
		String hostname = getItestProperty(hostLabel + ".hostname");
		String amiId = getItestProperty(hostLabel + ".amiId");

		checkState(hostname == null || amiId == null, "Both a hostname (" + hostname + ") and an AMI id (" + amiId + ") have been specified for host label " + hostLabel);

		if (hostname != null) {
			logger.info("Using existing host for integration tests on {}", hostLabel);
			return new ExistingItestHost(hostLabel);
		}

		if (amiId != null) {
			if (disableEc2) {
				throw new IllegalStateException("Only an AMI ID (" + amiId + ") has been specified for host label " + hostLabel
				        + ", but EC2 itest hosts are not available for this test.");
			}
			logger.info("Using Amazon EC2 for integration tests on {}", hostLabel);
			return new Ec2ItestHost(hostLabel, amiId);
		}

		throw new IllegalStateException("Neither a hostname (" + hostname + ") nor an AMI id (" + amiId + ") have been specified for host label " + hostLabel);
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
				logger.warn("File itest.properties not found in the current directory, using system properties only");
			}
			return itestProperties;
		} catch (IOException exc) {
			throw new RuntimeException("Cannot read itest.properties", exc);
		}
	}

	public static String getRequiredItestProperty(String key) {
		String value = getItestProperty(key);
		if(value == null) {
			throw new IllegalStateException("Required property " + key + " is not specified in itest.properties or as a system property");
		}
		return value;
	}

	public static String getItestProperty(String key) {
		return getItestProperty(key, null);
	}

	public static String getItestProperty(String key, String defaultValue) {
		String value = System.getProperty(key);
		if (value != null) {
			return value;
		} else {
			return itestProperties.getProperty(key, defaultValue);
		}
	}

}
