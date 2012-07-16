package com.xebialabs.overthere.cifs.winrm.soap;

import org.dom4j.Element;
import org.dom4j.QName;

import java.net.URI;
import java.util.List;

import static com.xebialabs.overthere.cifs.winrm.Namespaces.NS_ADDRESSING;
import static com.xebialabs.overthere.cifs.winrm.Namespaces.NS_WSMAN_DMTF;
import static com.xebialabs.overthere.cifs.winrm.Namespaces.NS_WSMAN_MSFT;
import static com.xebialabs.overthere.cifs.winrm.soap.Soapy.mustUnderstand;
import static com.xebialabs.overthere.cifs.winrm.soap.Soapy.needNotUnderstand;

public class HeaderBuilder {
	private Element header;

	public HeaderBuilder(Element header) {
		this.header = header;
	}

	public HeaderBuilder to(URI address) {
		header.addElement(QName.get("To", NS_ADDRESSING)).addText(address.toString());
		return this;
	}

	public HeaderBuilder replyTo(URI address) {
		final Element replyTo = header.addElement(QName.get("ReplyTo", NS_ADDRESSING));
		mustUnderstand(replyTo.addElement(QName.get("Address", NS_ADDRESSING))).addText(address.toString());
		return this;
	}

	public HeaderBuilder maxEnvelopeSize(int size) {
		mustUnderstand(header.addElement(QName.get("MaxEnvelopeSize", NS_WSMAN_DMTF))).addText("" + size);
		return this;
	}

	public HeaderBuilder withId(String id) {
		header.addElement(QName.get("MessageID", NS_ADDRESSING)).addText(id);
		return this;
	}

	public HeaderBuilder withLocale(String locale) {
		needNotUnderstand(header.addElement(QName.get("Locale", NS_WSMAN_DMTF))).addAttribute("xml:lang", locale);
		needNotUnderstand(header.addElement(QName.get("DataLocale", NS_WSMAN_MSFT))).addAttribute("xml:lang", locale);
		return this;
	}

	public HeaderBuilder withTimeout(String timeout) {
		header.addElement(QName.get("OperationTimeout", NS_WSMAN_DMTF)).addText(timeout);
		return this;
	}

	public HeaderBuilder withAction(URI uri) {
		mustUnderstand(header.addElement(QName.get("Action", NS_ADDRESSING))).addText(uri.toString());
		return this;
	}

	// TODO maybe split this up with a SelectorBuilder?
	public HeaderBuilder withShellId(String shellId) {
		header.addElement(QName.get("SelectorSet", NS_WSMAN_DMTF)).addElement(QName.get("Selector", NS_WSMAN_DMTF)).addAttribute("Name", "ShellId").addText(shellId);
		return this;
	}

	public HeaderBuilder withResourceURI(URI uri) {
		mustUnderstand(header.addElement(QName.get("ResourceURI", NS_WSMAN_DMTF))).addText(uri.toString());
		return this;
	}

	public HeaderBuilder withOptionSet(List<KeyValuePair> options) {
		final Element optionSet = header.addElement(QName.get("OptionSet", NS_WSMAN_DMTF));
		for (KeyValuePair p : options) {
			optionSet.addElement(QName.get("Option", NS_WSMAN_DMTF)).addAttribute("Name", p.getKey()).addText(p.getValue());
		}
		return this;
	}
}
