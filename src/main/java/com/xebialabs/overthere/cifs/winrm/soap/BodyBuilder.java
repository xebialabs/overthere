package com.xebialabs.overthere.cifs.winrm.soap;

import org.dom4j.Element;

public class BodyBuilder {
	private Element body;

	public BodyBuilder(Element body) {
		this.body = body;
	}

	public void setContent(Element content) {
		body.add(content);
	}
}
