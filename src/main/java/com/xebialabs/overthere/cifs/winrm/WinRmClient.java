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
    private int exitValue = -1;

    private int chunk = 0;

    public WinRmClient(HttpConnector connector, URL targetURL) {
        this.connector = connector;
        this.targetURL = targetURL;
    }

    public void createShell() {
        logger.debug("Sending WinRM Create Shell request");

        final Element bodyContent = DocumentHelper.createElement(QName.get("Shell", Namespaces.NS_WIN_SHELL));
        bodyContent.addElement(QName.get("InputStreams", Namespaces.NS_WIN_SHELL)).addText("stdin");
        bodyContent.addElement(QName.get("OutputStreams", Namespaces.NS_WIN_SHELL)).addText("stdout stderr");
        final Document requestDocument = getRequestDocument(Action.WS_ACTION, ResourceURI.RESOURCE_URI_CMD, OptionSet.OPEN_SHELL, bodyContent);

        Document responseDocument = sendMessage(requestDocument, SoapAction.SHELL);

        shellId = getFirstElement(responseDocument, ResponseExtractor.SHELL_ID);

        logger.debug("Received WinRM Create Shell response: shell with ID [{}] start created", shellId);
    }

    public void executeCommand(String command) {
        logger.debug("Sending WinRM Execute Command request to shell [{}]", shellId);

        final Element bodyContent = DocumentHelper.createElement(QName.get("CommandLine", Namespaces.NS_WIN_SHELL));
        String encoded = "\"" + command + "\"";
        bodyContent.addElement(QName.get("Command", Namespaces.NS_WIN_SHELL)).addText(encoded);
        final Document requestDocument = getRequestDocument(Action.WS_COMMAND, ResourceURI.RESOURCE_URI_CMD, OptionSet.RUN_COMMAND, bodyContent);

        Document responseDocument = sendMessage(requestDocument, SoapAction.COMMAND_LINE);
        
        commandId = getFirstElement(responseDocument, ResponseExtractor.COMMAND_ID);

        logger.debug("Received WinRM Execute Command response to shell [{}]: command with ID [{}] was started", shellId, commandId);
    }

    public boolean receiveOutput(OutputStream stdout, OutputStream stderr) throws IOException {
        logger.debug("Sending WinRM Receive Output request for command [{}] in shell [{}]", commandId, shellId);

        final Element bodyContent = DocumentHelper.createElement(QName.get("Receive", Namespaces.NS_WIN_SHELL));
        bodyContent.addElement(QName.get("DesiredStream", Namespaces.NS_WIN_SHELL)).addAttribute("CommandId", commandId).addText("stdout stderr");
        final Document requestDocument = getRequestDocument(Action.WS_RECEIVE, ResourceURI.RESOURCE_URI_CMD, null, bodyContent);

        Document responseDocument = sendMessage(requestDocument, SoapAction.RECEIVE);

        logger.debug("Received WinRM Receive Output response for command [{}] in shell [{}]", commandId, shellId);
        
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
            logger.trace("Found CommandState element with State=Done, parsing exit code and returning false.");
            parseExitCode(responseDocument);
            return false;
        } else {
            logger.trace("Did not find CommandState element with State=Done, returning true.");
            return true;
        }
    }

    public void sendInput(byte[] buf) throws IOException {
        logger.debug("Sending WinRM Send Input request for command [{}} in shell [{}]", commandId, shellId);

        final Element bodyContent = DocumentHelper.createElement(QName.get("Send", Namespaces.NS_WIN_SHELL));
        final Base64 base64 = new Base64();
        bodyContent.addElement(QName.get("Stream", Namespaces.NS_WIN_SHELL)).addAttribute("Name", "stdin").addAttribute("CommandId", commandId).addText(base64.encodeAsString(buf));
        final Document requestDocument = getRequestDocument(Action.WS_SEND, ResourceURI.RESOURCE_URI_CMD, null, bodyContent);
        sendMessage(requestDocument, SoapAction.SEND);

        logger.debug("Sent WinRM Send Input request for command [{}} in shell [{}]", commandId, shellId);
    }

    public void signal() {
        if (commandId == null) {
            logger.warn("Not sending WinRM Signal request in shell [{}] because there is no running command", shellId);
            return;
        }

        logger.debug("Sending WinRM Signal request for command [{}} in shell [{}]", commandId, shellId);

        final Element bodyContent = DocumentHelper.createElement(QName.get("Signal", Namespaces.NS_WIN_SHELL)).addAttribute("CommandId", commandId);
        bodyContent.addElement(QName.get("Code", Namespaces.NS_WIN_SHELL)).addText("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/signal/terminate");
        final Document requestDocument = getRequestDocument(Action.WS_SIGNAL, ResourceURI.RESOURCE_URI_CMD, null, bodyContent);
        sendMessage(requestDocument, SoapAction.SIGNAL);

        logger.debug("Sent WinRM Signal request for command [{}} in shell [{}]", commandId, shellId);
    }

    public void deleteShell() {
        if (shellId == null) {
            logger.warn("Not sending WinRM Delete Shell request because there is no shell");
            return;
        }

        logger.debug("Sending WinRM Delete Shell request for shell [{}]", shellId);

        final Document requestDocument = getRequestDocument(Action.WS_DELETE, ResourceURI.RESOURCE_URI_CMD, null, null);
        sendMessage(requestDocument, null);

        logger.debug("Sent WinRM Delete Shell request for shell [{}]", shellId);
    }

    private void parseExitCode(Document responseDocument) {
        try {
            logger.trace("Parsing exit code");
            String exitCode = getFirstElement(responseDocument, ResponseExtractor.EXIT_CODE);
            logger.trace("Found exit code [{}]", exitCode);
            try {
                exitValue = Integer.parseInt(exitCode);
            } catch(NumberFormatException exc) {
                logger.error("Cannot parse exit code [{}], setting it to -1", exc);
                exitValue = -1;
            }
        } catch (Exception exc) {
            logger.trace("Exit code not found,");
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

    private Document getRequestDocument(Action action, ResourceURI resourceURI, OptionSet optionSet, Element bodyContent) {
        SoapMessageBuilder message = Soapy.newMessage();
        SoapMessageBuilder.EnvelopeBuilder envelope = message.envelope();
        try {
            addHeaders(envelope, action, resourceURI, optionSet);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        BodyBuilder body = envelope.body();
        if (bodyContent != null)
            body.setContent(bodyContent);

        return message.getDocument();
    }

    private void addHeaders(SoapMessageBuilder.EnvelopeBuilder envelope, Action action, ResourceURI resourceURI, OptionSet optionSet)
        throws URISyntaxException {
        HeaderBuilder header = envelope.header();
        header.to(targetURL.toURI()).replyTo(new URI("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous"));
        header.maxEnvelopeSize(envelopSize);
        header.withId(getUUID());
        header.withLocale(locale);
        header.withTimeout(timeout);
        header.withAction(action.getUri());
        if (shellId != null) {
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
