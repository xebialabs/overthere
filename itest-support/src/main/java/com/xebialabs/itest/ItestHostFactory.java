/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.itest;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newLinkedHashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItestHostFactory {

	public static final String HOSTNAME_PROPERTY_SUFFIX = ".hostname";

	public static final String AMI_ID_PROPERTY_SUFFIX = ".amiId";

	public static final String AWS_ENDPOINT_PROPERTY = "aws.endpoint";

	public static final String AWS_ENDPOINT_DEFAULT = "https://ec2.amazonaws.com";

	public static final String AWS_ACCESS_KEY_PROPERTY = "aws.accessKey";

	public static final String AWS_SECRET_KEY_PROPERTY = "aws.secretKey";

	public static final String AMI_AVAILABILITY_ZONE_PROPERTY_SUFFIX = ".amiAvailabilityZone";
	
	public static final String AMI_INSTANCE_TYPE_PROPERTY_SUFFIX = ".amiInstanceType";

	public static final String AMI_SECURITY_GROUP_PROPERTY_SUFFIX = ".amiSecurityGroup";

	public static final String AMI_KEY_NAME_PROPERTY_SUFFIX = ".amiKeyName";

	public static final String AMI_BOOT_SECONDS_PROPERTY_SUFFIX = ".amiBootSeconds";

	public static final String TUNNEL_USERNAME_PROPERTY_SUFFIX = ".tunnel.username";

	public static final String TUNNEL_PASSWORD_PROPERTY_SUFFIX = ".tunnel.password";

	public static final String TUNNEL_PORTS_PROPERTY_SUFFIX = ".tunnel.ports";

	// The field logger needs to be defined up here so that the static initialized below can use the logger
	public static Logger logger = LoggerFactory.getLogger(ItestHostFactory.class);

	private static Properties itestProperties;

	static {
		loadItestProperties();
	}

	public static ItestHost getItestHostThatDoesNotRequiresTeardown(String hostLabel) {
		return getItestHost(hostLabel, true);
	}

	public static ItestHost getItestHost(String hostLabel) {
		return getItestHost(hostLabel, false);
	}

	private static ItestHost getItestHost(String hostLabel, boolean disableEc2) {
		ItestHost ih = createItestHost(hostLabel, disableEc2);
		ih = wrapItestHost(hostLabel, ih);
		return ih;
	}

	protected static ItestHost createItestHost(String hostLabel, boolean disableEc2) {
		String hostname = getItestProperty(hostLabel + HOSTNAME_PROPERTY_SUFFIX);
		if (hostname != null) {
			logger.info("Using existing host for integration tests on {}", hostLabel);
			return new ExistingItestHost(hostLabel);
		}

		String amiId = getItestProperty(hostLabel + AMI_ID_PROPERTY_SUFFIX);
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

	private static ItestHost wrapItestHost(String hostLabel, ItestHost actualItestHost) {
		String tunnelUsername = getItestProperty(hostLabel + TUNNEL_USERNAME_PROPERTY_SUFFIX);
		if(tunnelUsername == null) {
			return actualItestHost;
		}
		
		logger.info("Starting SSH tunnels for integration tests on {}", hostLabel);

		String tunnelPassword = getRequiredItestProperty(hostLabel + TUNNEL_PASSWORD_PROPERTY_SUFFIX);
		String ports = getRequiredItestProperty(hostLabel + TUNNEL_PORTS_PROPERTY_SUFFIX);
		Map<Integer, Integer> portForwardMap = parsePortsProperty(ports);
		return new TunneledItestHost(actualItestHost, tunnelUsername, tunnelPassword, portForwardMap);
    }

	private static Map<Integer, Integer> parsePortsProperty(String ports) {
		Map<Integer, Integer> portForwardMap = newLinkedHashMap();
		StringTokenizer toker = new StringTokenizer(ports, ",");
		while(toker.hasMoreTokens()) {
			String[] localAndRemotePort = toker.nextToken().split(":");
			checkArgument(localAndRemotePort.length == 2, "Property value \"%s\" does not have the right format, e.g. 2222:22,1445:445", ports);
			try {
				int localPort = Integer.parseInt(localAndRemotePort[0]);
				int remotePort = Integer.parseInt(localAndRemotePort[1]);
				portForwardMap.put(remotePort, localPort);
			} catch(NumberFormatException exc) {
				throw new IllegalArgumentException("Property value \"" + ports + "\" does not have the right format, e.g. 2222:22,1445:445", exc);
			}
		}
		return portForwardMap;
    }

	private static void loadItestProperties() {
		try {
			itestProperties = new Properties();
			loadItestPropertiesFromClasspath();
			loadItestPropertiesFromHomeDirectory();
			loadItestPropertiesFromCurrentDirectory();
		} catch (IOException exc) {
			throw new RuntimeException("Cannot load itest.properties", exc);
		}
	}

	private static void loadItestPropertiesFromClasspath() throws IOException {
	    URL itestPropertiesResource = Thread.currentThread().getContextClassLoader().getResource("itest.properties");
	    if(itestPropertiesResource != null) {
	    	InputStream in = itestPropertiesResource.openStream();
	    	try {
	    		logger.info("Loading {}", itestPropertiesResource);
	    		itestProperties.load(in);
	    	} finally {
	    		in.close();
	    	}
	    } else {
	    	logger.warn("File itest.properties not found on classpath.");
	    }
    }

	private static void loadItestPropertiesFromHomeDirectory() throws FileNotFoundException, IOException {
	    loadItestPropertiesFromFile(new File(System.getProperty("user.home"), ".itest/itest.properties"));
    }

	private static void loadItestPropertiesFromCurrentDirectory() throws FileNotFoundException, IOException {
	    loadItestPropertiesFromFile(new File("itest.properties"));
    }

	private static void loadItestPropertiesFromFile(File itestPropertiesFile) throws FileNotFoundException, IOException {
	    if (itestPropertiesFile.exists()) {
	    	FileInputStream in = new FileInputStream(itestPropertiesFile);
	    	try {
	    		logger.info("Loading {}", itestPropertiesFile.getAbsolutePath());
	    		itestProperties.load(in);
	    	} finally {
	    		in.close();
	    	}
	    } else {
	    	logger.warn("File {} not found.", itestPropertiesFile.getAbsolutePath());
	    }
    }

	public static String getRequiredItestProperty(String key) {
		String value = getItestProperty(key);
		checkState(value != null, "Required property %s is not specified as a system property or in itest.properties which can be placed in the current working directory, in ~/.itest or on the classpath", key);
		return value;
	}

	public static String getItestProperty(String key) {
		return getItestProperty(key, null);
	}

	public static String getItestProperty(String key, String defaultValue) {
		String value = System.getProperty(key);
		if (value == null) {
			value = itestProperties.getProperty(key, defaultValue);
		}
		if(logger.isTraceEnabled()) {
			if(value == null) {
				logger.trace("Itest property {} is null", key);
			} else {
				logger.trace("Itest property {}={}", key, key.endsWith(TUNNEL_PASSWORD_PROPERTY_SUFFIX) ? "********" : value);
			}
		}
		return value;
	}

}

