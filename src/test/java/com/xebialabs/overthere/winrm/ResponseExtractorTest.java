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
package com.xebialabs.overthere.winrm;

import com.google.common.io.Resources;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.jdom2.JDOMException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResponseExtractorTest {


    @Test
    public void checkReturnExitCodeWinRmV1() throws DocumentException {
        Document doc = getRequestDocument("winrm/winrm-exitcode-test_10.xml");
        String exitCode = getFirstElement(doc, ResponseExtractor.EXIT_CODE.getXPath());
        assertThat(exitCode, equalTo("12"));
    }

    @Test
    public void checkReturnExitCodeWinRmV2() throws DocumentException {
        Document doc = getRequestDocument("winrm/winrm-exitcode-test_20.xml");
        String exitCode = getFirstElement(doc, ResponseExtractor.EXIT_CODE.getXPath());
        assertThat(exitCode, equalTo("16"));
    }

    @SuppressWarnings("unchecked")
    private static String getFirstElement(Document doc, XPath xpath) {
        final List<Node> nodes = xpath.selectNodes(doc);
        assertThat(nodes.isEmpty(), equalTo(false));
        assertThat(nodes.size(), equalTo(1));
        final Node next = nodes.iterator().next();
        return next.getText();
    }

    private Document getRequestDocument(String path) throws DocumentException {
        URL resource = Resources.getResource(path);
        SAXReader reader = new SAXReader();
        Document responseDocument = reader.read(resource);
        return responseDocument;
    }
}
