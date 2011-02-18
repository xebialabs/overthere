package com.xebialabs.overthere.local;

import com.xebialabs.overthere.AbstractHostSessionSpecification;
import com.xebialabs.overthere.OperatingSystemFamily;

public class LocalHostSessionSpecification extends AbstractHostSessionSpecification {

	public LocalHostSessionSpecification(OperatingSystemFamily os, String temporaryDirectoryPath) {
	    super(os, temporaryDirectoryPath);
    }

}
