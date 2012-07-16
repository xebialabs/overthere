package com.xebialabs.overthere.cifs.winrm.soap;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

import static com.xebialabs.overthere.cifs.winrm.Namespaces.NS_SOAP_ENV;

public class SoapMessageBuilder {

	private Document doc = DocumentHelper.createDocument();

	public EnvelopeBuilder envelope() {
		Element envelope = doc.addElement(QName.get("Envelope", NS_SOAP_ENV));
		return new EnvelopeBuilder(envelope);
	}

	public class EnvelopeBuilder {
		private Element envelope;

		public EnvelopeBuilder(Element envelope) {
			this.envelope = envelope;
		}

		public HeaderBuilder header() {
			Element header = envelope.addElement(QName.get("Header", NS_SOAP_ENV));
			return new HeaderBuilder(header);
		}

		public BodyBuilder body() {
			Element body = envelope.addElement(QName.get("Body", NS_SOAP_ENV));
			return new BodyBuilder(body);
		}
	}

	public Document getDocument() {
		return doc;
	}
}
