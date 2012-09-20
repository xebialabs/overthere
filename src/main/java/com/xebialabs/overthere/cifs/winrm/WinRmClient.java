/**
 * Copyright (c) 2008, 2012, XebiaLabs B.V., All rights reserved.
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
package com.xebialabs.overthere.cifs.winrm;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.cifs.winrm.exception.WinRMRuntimeIOException;
import com.xebialabs.overthere.cifs.winrm.soap.Action;
import com.xebialabs.overthere.cifs.winrm.soap.BodyBuilder;
import com.xebialabs.overthere.cifs.winrm.soap.HeaderBuilder;
import com.xebialabs.overthere.cifs.winrm.soap.OptionSet;
import com.xebialabs.overthere.cifs.winrm.soap.ResourceURI;
import com.xebialabs.overthere.cifs.winrm.soap.SoapAction;
import com.xebialabs.overthere.cifs.winrm.soap.SoapMessageBuilder;
import com.xebialabs.overthere.cifs.winrm.soap.Soapy;

import static com.xebialabs.overthere.cifs.winrm.Namespaces.NS_WIN_SHELL;

/**
 * See http://msdn.microsoft.com/en-us/library/cc251731(v=prot.10).aspx for some examples of how the WS-MAN protocol works on Windows
 */
public class WinRmClient {

    private final URL targetURL;
    private final HttpConnector connector;

    private String timeout;
    private int envelopSize;
    private String locale;

    private String shellId;
    private String commandId;
    private int exitValue;

    private int chunk = 0;

    public WinRmClient(HttpConnector connector, URL targetURL) {
        this.connector = connector;
        this.targetURL = targetURL;
    }

    public void startCmd(String command) {
        shellId = createShell();
        commandId = executeCommand(command);
    }
    
    private String createShell() {
        logger.debug("createShell");

        final Element bodyContent = DocumentHelper.createElement(QName.get("Shell", NS_WIN_SHELL));
        bodyContent.addElement(QName.get("InputStreams", NS_WIN_SHELL)).addText("stdin");
        bodyContent.addElement(QName.get("OutputStreams", NS_WIN_SHELL)).addText("stdout stderr");

        final Document requestDocument = getRequestDocument(Action.WS_ACTION, ResourceURI.RESOURCE_URI_CMD, OptionSet.OPEN_SHELL, null, bodyContent);
        Document responseDocument = sendMessage(requestDocument, SoapAction.SHELL);

        return getFirstElement(responseDocument, ResponseExtractor.SHELL_ID);

    }

    private String executeCommand(String command) {
        logger.debug("runCommand shellId {} command {}", shellId, command);
        final Element bodyContent = DocumentHelper.createElement(QName.get("CommandLine", NS_WIN_SHELL));

        String encoded = "\"" + command + "\"";

        logger.info("Encoded command is {}", encoded);

        bodyContent.addElement(QName.get("Command", NS_WIN_SHELL)).addText(encoded);

        final Document requestDocument = getRequestDocument(Action.WS_COMMAND, ResourceURI.RESOURCE_URI_CMD, OptionSet.RUN_COMMAND, shellId, bodyContent);
        Document responseDocument = sendMessage(requestDocument, SoapAction.COMMAND_LINE);

        return getFirstElement(responseDocument, ResponseExtractor.COMMAND_ID);
    }


    public boolean receiveOutput(OutputStream stdout, OutputStream stderr) throws IOException {
        logger.debug("receiveOutput shellId {} commandId {} ", shellId, commandId);
        final Element bodyContent = DocumentHelper.createElement(QName.get("Receive", NS_WIN_SHELL));
        bodyContent.addElement(QName.get("DesiredStream", NS_WIN_SHELL)).addAttribute("CommandId", commandId).addText("stdout stderr");
        final Document requestDocument = getRequestDocument(Action.WS_RECEIVE, ResourceURI.RESOURCE_URI_CMD, null, shellId, bodyContent);

        Document responseDocument = sendMessage(requestDocument, SoapAction.RECEIVE);
        handleStream(responseDocument, ResponseExtractor.STDOUT, stdout);
        handleStream(responseDocument, ResponseExtractor.STDERR, stderr);

        if (chunk == 0) {
            parseExitCode(responseDocument);
        }
        chunk++;

        /*
         * We may need to get additional output if the stream has not finished. The CommandState will change from
         * Running to Done like so:
         * 
         * @example
         * 
         * from... <rsp:CommandState CommandId="..."
         * State="http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Running"/> to...
         * <rsp:CommandState CommandId="..."
         * State="http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Done">
         * <rsp:ExitCode>0</rsp:ExitCode> </rsp:CommandState>
         */
        final List<?> list = ResponseExtractor.STREAM_DONE.getXPath().selectNodes(responseDocument);
        if (!list.isEmpty()) {
            parseExitCode(responseDocument);
            return false;
        }

        return true;
    }

    public void signal() {
        if (commandId == null)
            return;
        logger.debug("Signal shellId {} commandId {} ", shellId, commandId);
        final Element bodyContent = DocumentHelper.createElement(QName.get("Signal", NS_WIN_SHELL)).addAttribute("CommandId", commandId);
        bodyContent.addElement(QName.get("Code", NS_WIN_SHELL)).addText("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/signal/terminate");
        final Document requestDocument = getRequestDocument(Action.WS_SIGNAL, ResourceURI.RESOURCE_URI_CMD, null, shellId, bodyContent);
        sendMessage(requestDocument, SoapAction.SIGNAL);
    }

    public void deleteShell() {
        if (shellId == null)
            return;
        logger.debug("DeleteShell shellId {}", shellId);
        final Document requestDocument = getRequestDocument(Action.WS_DELETE, ResourceURI.RESOURCE_URI_CMD, null, shellId, null);
        sendMessage(requestDocument, null);
    }

    private void parseExitCode(Document responseDocument) {
        try {
            String exitCode = getFirstElement(responseDocument, ResponseExtractor.EXIT_CODE);
            logger.debug("exit code {}", exitCode);
            try {
                exitValue = Integer.parseInt(exitCode);
            } catch(NumberFormatException exc) {
                logger.error("Cannot parse exit code [{}], setting it to -1", exc);
                exitValue = -1;
            }
        } catch (Exception exc) {
            logger.debug("Exit code not found, not processing it");
        }
    }

    private static void handleStream(Document responseDocument, ResponseExtractor stream, OutputStream out) throws IOException {
        @SuppressWarnings("unchecked") final List<Element> streams = stream.getXPath().selectNodes(responseDocument);
        if (!streams.isEmpty()) {
            final Base64 base64 = new Base64();
            Iterator<Element> itStreams = streams.iterator();
            while (itStreams.hasNext()) {
                Element e = itStreams.next();
                // TODO check performance with http://www.iharder.net/current/java/base64/
                final byte[] decode = base64.decode(e.getText());
                out.write(decode);
            }
        }

    }

    private static String getFirstElement(Document doc, ResponseExtractor extractor) {
        @SuppressWarnings("unchecked") final List<Element> nodes = extractor.getXPath().selectNodes(doc);
        if (nodes.isEmpty())
            throw new RuntimeException("Cannot find " + extractor.getXPath() + " in " + toString(doc));

        final Element next = nodes.iterator().next();
        return next.getText();
    }

    private Document sendMessage(Document requestDocument, SoapAction soapAction) {
        return connector.sendMessage(requestDocument, soapAction);
    }

    private Document getRequestDocument(Action action, ResourceURI resourceURI, OptionSet optionSet, String shelId, Element bodyContent) {
        SoapMessageBuilder message = Soapy.newMessage();
        SoapMessageBuilder.EnvelopeBuilder envelope = message.envelope();
        try {
            addHeaders(envelope, action, resourceURI, optionSet, shelId);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        BodyBuilder body = envelope.body();
        if (bodyContent != null)
            body.setContent(bodyContent);

        return message.getDocument();
    }

    private void addHeaders(SoapMessageBuilder.EnvelopeBuilder envelope, Action action, ResourceURI resourceURI, OptionSet optionSet, String shelId)
        throws URISyntaxException {
        HeaderBuilder header = envelope.header();
        header.to(targetURL.toURI()).replyTo(new URI("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"));
        header.maxEnvelopeSize(envelopSize);
        header.withId(getUUID());
        header.withLocale(locale);
        header.withTimeout(timeout);
        header.withAction(action.getUri());
        if (shelId != null) {
            header.withShellId(shellId);
        }
        header.withResourceURI(resourceURI.getUri());
        if (optionSet != null) {
            header.withOptionSet(optionSet.getKeyValuePairs());
        }
    }

    private static String toString(Document doc) {
        StringWriter stringWriter = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(stringWriter, OutputFormat.createPrettyPrint());
        try {
            xmlWriter.write(doc);
            xmlWriter.close();
        } catch (IOException e) {
            throw new WinRMRuntimeIOException("error ", e);
        }
        return stringWriter.toString();
    }

    private static String getUUID() {
        return "uuid:" + UUID.randomUUID().toString().toUpperCase();
    }

    public int exitValue() {
        return exitValue;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public int getEnvelopSize() {
        return envelopSize;
    }

    public void setEnvelopSize(int envelopSize) {
        this.envelopSize = envelopSize;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public URL getTargetURL() {
        return targetURL;
    }

    private static Logger logger = LoggerFactory.getLogger(WinRmClient.class);

}
