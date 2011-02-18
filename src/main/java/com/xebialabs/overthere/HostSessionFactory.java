package com.xebialabs.overthere;

public interface HostSessionFactory {

	boolean canHandle(HostSessionSpecification spec);

	HostSession createHostSession(HostSessionSpecification spec);

}
