package com.xebialabs.overthere.cifs.winrm;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.io.Resources;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ResponseExtractorTest {


    @Test
    public void checkReturnExitCodeWinRmV1() throws IOException, DocumentException {
        Document doc = getRequestDocument("winrm/winrm-exitcode-test_10.xml");
        String exitCode = getFirstElement(doc,ResponseExtractor.EXIT_CODE.getXPath());
        assertThat(exitCode, equalTo("12"));
    }

    @Test
    public void checkReturnExitCodeWinRmV2() throws IOException, DocumentException {
        Document doc = getRequestDocument("winrm/winrm-exitcode-test_20.xml");
        String exitCode = getFirstElement(doc,ResponseExtractor.EXIT_CODE.getXPath());
        assertThat(exitCode, equalTo("16"));
    }

    @SuppressWarnings("unchecked")
    private static String getFirstElement(Document doc, XPath xpath) {
        final List<Element> nodes = xpath.selectNodes(doc);
        assertThat(nodes.isEmpty(),equalTo(false));
        assertThat(nodes.size(),equalTo(1));
        final Element next = nodes.iterator().next();
        return next.getText();
    }

    private Document getRequestDocument(String path) throws DocumentException, IOException {
        URL resource = Resources.getResource(path);
        List<String> list = Resources.readLines(resource, Charset.defaultCharset());
        Document responseDocument = DocumentHelper.parseText(Joiner.on("\n|").join(list));
        return responseDocument;
    }
}
