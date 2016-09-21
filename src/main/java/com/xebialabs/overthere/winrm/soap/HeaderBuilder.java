/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere.winrm.soap;

import java.net.URI;
import java.util.List;
import org.dom4j.Element;
import org.dom4j.QName;

import static com.xebialabs.overthere.winrm.Namespaces.NS_ADDRESSING;
import static com.xebialabs.overthere.winrm.Namespaces.NS_WSMAN_DMTF;
import static com.xebialabs.overthere.winrm.Namespaces.NS_WSMAN_MSFT;
import static com.xebialabs.overthere.winrm.soap.Soapy.mustUnderstand;
import static com.xebialabs.overthere.winrm.soap.Soapy.needNotUnderstand;

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
        header.addElement(QName.get("SelectorSet", NS_WSMAN_DMTF)).addElement(QName.get("Selector", NS_WSMAN_DMTF)).addAttribute("Name", "ShellId")
                .addText(shellId);
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
