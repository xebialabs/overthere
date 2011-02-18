package com.xebialabs.overthere;

public abstract class AbstractHostSessionSpecification implements HostSessionSpecification {

	private OperatingSystemFamily os;

	private String temporaryDirectoryPath;

	public AbstractHostSessionSpecification(OperatingSystemFamily os, String temporaryDirectoryPath) {
		this.os = os;
		this.temporaryDirectoryPath = temporaryDirectoryPath;
	}

	public OperatingSystemFamily getOs() {
		return os;
	}

	public String getTemporaryDirectoryPath() {
		return temporaryDirectoryPath;
	}

}
