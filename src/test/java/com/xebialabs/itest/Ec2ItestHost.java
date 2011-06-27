package com.xebialabs.itest;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

class Ec2ItestHost implements ItestHost {

	public static final String AWS_ENDPOINT = "aws.endpoint";

	public static final String AWS_ENDPOINT_DEFAULT = "https://ec2.amazonaws.com";

	public static final String AWS_ACCESS_KEY = "aws.accessKey";

	public static final String AWS_SECRET_KEY = "aws.secretKey";

	public static final String AMI_INSTANCE_TYPE = "ami.instanceType";

	public static final String AMI_SECURITY_GROUP = "ami.securityGroup";

	public static final String AMI_KEY_NAME = "ami.keyName";
	
	public static final String AMI_BOOT_SECONDS = "ami.bootSeconds";

	private final String hostId;
	private final String amiId;
	private final String awsEndpointURL;
	private final String awsAccessKey;
	private final String awsSecretKey;
	private final String amiInstanceType;
	private final String amiSecurityGroup;
	private final String amiKeyName;
	private final int amiBootSeconds;

	private AmazonEC2Client ec2;
	private String instanceId;
	private String publicDnsAddress;

	public Ec2ItestHost(String hostId, String amiId, Properties itestProperties) {
		this.hostId = hostId;
		this.amiId = amiId;
		this.awsEndpointURL = itestProperties.getProperty(AWS_ENDPOINT, AWS_ENDPOINT_DEFAULT);
		this.awsAccessKey = checkNotNull(itestProperties.getProperty(AWS_ACCESS_KEY), "Required property %s missing", AWS_ACCESS_KEY);
		this.awsSecretKey = checkNotNull(itestProperties.getProperty(AWS_SECRET_KEY), "Required property %s missing", AWS_SECRET_KEY);
		this.amiInstanceType = itestProperties.getProperty(AMI_INSTANCE_TYPE);
		this.amiSecurityGroup = itestProperties.getProperty(AMI_SECURITY_GROUP);
		this.amiKeyName = itestProperties.getProperty(AMI_KEY_NAME);
		this.amiBootSeconds = Integer.valueOf(itestProperties.getProperty(AMI_BOOT_SECONDS, "120"));
		ec2 = new AmazonEC2Client(new BasicAWSCredentials(awsAccessKey, awsSecretKey));
		ec2.setEndpoint(awsEndpointURL);
	}

	@Override
	public void setup() {
		instanceId = runInstance();

		setInstanceName();

		publicDnsAddress = waitUntilRunningAndGetPublicDnsName();
		
		waitForAmiBoot();
	}

	@Override
	public void teardown() {
		ec2.terminateInstances(new TerminateInstancesRequest(newArrayList(instanceId)));
	}

	@Override
	public String getHostName() {
		return publicDnsAddress;
	}

	protected String runInstance() {
		RunInstancesRequest run = new RunInstancesRequest(amiId, 1, 1);
		run.withInstanceInitiatedShutdownBehavior("terminate");
		if (amiInstanceType != null) {
			run.withInstanceType(amiInstanceType);
		}
		if (amiSecurityGroup != null) {
			run.withSecurityGroups(amiSecurityGroup);
		}
		if (amiKeyName != null) {
			run.withKeyName(amiKeyName);
		}
		RunInstancesResult result = ec2.runInstances(run);
		return result.getReservation().getInstances().get(0).getInstanceId();
	}

	protected void setInstanceName() {
		ec2.createTags(new CreateTagsRequest(newArrayList(instanceId), newArrayList(new Tag("Name", hostId + " started at " + new Date()))));
	}

	public String waitUntilRunningAndGetPublicDnsName() {
		for (;;) {
			DescribeInstancesRequest describe = new DescribeInstancesRequest().withInstanceIds(newArrayList(instanceId));
			Instance instance = ec2.describeInstances(describe).getReservations().get(0).getInstances().get(0);
			if (instance.getState().getName().equals("running")) {
				return instance.getPublicDnsName();
			}

			logger.info("Instance {} is still {}. Waiting...", instanceId, instance.getState().getName());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

	}

	protected void waitForAmiBoot() {
	    try {
	    	logger.info("Waiting {} for the image to boot", amiBootSeconds);
	        Thread.sleep(amiBootSeconds * 1000);
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
    }

	private static final Logger logger = LoggerFactory.getLogger(Ec2ItestHost.class);

}
