package com.xebialabs.overthere.local;

import com.xebialabs.overthere.HostSession;
import com.xebialabs.overthere.HostSessionFactory;
import com.xebialabs.overthere.HostSessionSpecification;

public class LocalHostSessionFactory implements HostSessionFactory {

	public boolean canHandle(HostSessionSpecification spec) {
		return spec.getClass() == LocalHostSessionSpecification.class;
	}

	public HostSession createHostSession(HostSessionSpecification spec) {
		return new LocalHostSession((LocalHostSessionSpecification) spec);
	}

}
