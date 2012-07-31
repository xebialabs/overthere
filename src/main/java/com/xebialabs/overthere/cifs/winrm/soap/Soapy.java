package com.xebialabs.overthere.cifs.winrm.soap;

import com.xebialabs.overthere.cifs.winrm.exception.WinRMRuntimeIOException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

public class Soapy {
    private Soapy() {
    }

    public static SoapMessageBuilder newMessage() {
        return new SoapMessageBuilder();
    }

    static Element mustUnderstand(Element e) {
        return e.addAttribute("mustUnderstand", "true");
    }

    static Element needNotUnderstand(Element e) {
        return e.addAttribute("mustUnderstand", "false");
    }

    public static String toString(Document doc) {
        StringWriter stringWriter = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(stringWriter, OutputFormat.createPrettyPrint());
        try {
            xmlWriter.write(doc);
            xmlWriter.close();
        } catch (IOException e) {
            throw new WinRMRuntimeIOException("Cannnot convert XML to String ", e);
        }
        return stringWriter.toString();
    }

    static URI getUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
