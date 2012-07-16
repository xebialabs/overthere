/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.cifs.winrm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.xebialabs.overthere.cifs.winrm.soap.*;
import org.apache.commons.codec.binary.Base64;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.cifs.winrm.exception.WinRMRuntimeIOException;

import static com.xebialabs.overthere.cifs.winrm.Namespaces.NS_WIN_SHELL;

public class WinRmClient {

	private final URL targetURL;
	private final HttpConnector connector;

	private String timeout;
	private int envelopSize;
	private String locale;

	private String exitCode;
	private String shellId;
	private String commandId;

	private int chunk = 0;

	public WinRmClient(HttpConnector connector, URL targetURL) {
		this.connector = connector;
		this.targetURL = targetURL;
	}

	public void runCmd(String command, OverthereProcessOutputHandler handler) {
		try {
			shellId = openShell();
			commandId = runCommand(command);
			getCommandOutput(handler);
		} finally {
			cleanUp();
			closeShell();
		}
	}


	private void closeShell() {
		if (shellId == null)
			return;
		logger.debug("closeShell shellId {}", shellId);
		final Document requestDocument = getRequestDocument(Action.WS_DELETE, ResourceURI.RESOURCE_URI_CMD, null, shellId, null);
        sendMessage(requestDocument, null);
	}

	private void cleanUp() {
		if (commandId == null)
			return;
		logger.debug("cleanUp shellId {} commandId {} ", shellId, commandId);
		final Element bodyContent = DocumentHelper.createElement(QName.get("Signal", NS_WIN_SHELL)).addAttribute("CommandId", commandId);
		bodyContent.addElement(QName.get("Code", NS_WIN_SHELL)).addText("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/signal/terminate");
		final Document requestDocument = getRequestDocument(Action.WS_SIGNAL, ResourceURI.RESOURCE_URI_CMD, null, shellId, bodyContent);
        sendMessage(requestDocument, SoapAction.SIGNAL);
	}

	private void getCommandOutput(OverthereProcessOutputHandler handler) {
		logger.debug("getCommandOutput shellId {} commandId {} ", shellId, commandId);
		final Element bodyContent = DocumentHelper.createElement(QName.get("Receive", NS_WIN_SHELL));
		bodyContent.addElement(QName.get("DesiredStream", NS_WIN_SHELL)).addAttribute("CommandId", commandId).addText("stdout stderr");
		final Document requestDocument = getRequestDocument(Action.WS_RECEIVE, ResourceURI.RESOURCE_URI_CMD, null, shellId, bodyContent);

		for (;;) {
			Document responseDocument = sendMessage(requestDocument, SoapAction.RECEIVE);
			String stdout = handleStream(responseDocument, ResponseExtractor.STDOUT);
			BufferedReader stdoutReader = new BufferedReader(new StringReader(stdout));
			try {
				for(;;) {
					String line = stdoutReader.readLine();
					if(line == null) {
						break;
					}
					handler.handleOutputLine(line);
				}
			} catch(IOException exc) {
				throw new RuntimeIOException("Unexpected I/O exception while reading stdout", exc);
			}

			String stderr = handleStream(responseDocument, ResponseExtractor.STDERR);
			BufferedReader stderrReader = new BufferedReader(new StringReader(stderr));
			try {
				for(;;) {
					String line = stderrReader.readLine();
					if(line == null) {
						break;
					}
					handler.handleErrorLine(line);
				}
			} catch(IOException exc) {
				throw new RuntimeIOException("Unexpected I/O exception while reading stderr", exc);
			}

			if (chunk == 0) {
				try {
					exitCode = getFirstElement(responseDocument, ResponseExtractor.EXIT_CODE);
					logger.info("exit code {}", exitCode);
				} catch (Exception e) {
					logger.debug("not found");
				}
			}
			chunk++;

			/* We may need to get additional output if the stream has not finished.
										The CommandState will change from Running to Done like so:
										@example

										 from...
										 <rsp:CommandState CommandId="..." State="http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Running"/>
										 to...
										 <rsp:CommandState CommandId="..." State="http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Done">
											 <rsp:ExitCode>0</rsp:ExitCode>
										 </rsp:CommandState>
									 */
			final List<?> list = ResponseExtractor.STREAM_DONE.getXPath().selectNodes(responseDocument);
			if (!list.isEmpty()) {
				exitCode = getFirstElement(responseDocument, ResponseExtractor.EXIT_CODE);
				logger.info("exit code {}", exitCode);
				break;
			}
		}


		logger.debug("all the command output has been fetched (chunk={})", chunk);


	}

	private static String handleStream(Document responseDocument, ResponseExtractor stream) {
		StringBuffer buffer = new StringBuffer();
		@SuppressWarnings("unchecked")
        final List<Element> streams = stream.getXPath().selectNodes(responseDocument);
		if (!streams.isEmpty()) {
			final Base64 base64 = new Base64();
			Iterator<Element> itStreams = streams.iterator();
			while (itStreams.hasNext()) {
				Element e = itStreams.next();
				//TODO check performance with http://www.iharder.net/current/java/base64/
				final byte[] decode = base64.decode(e.getText());
				buffer.append(new String(decode));
			}
		}
		logger.debug("handleStream {} buffer {}", stream, buffer);
		return buffer.toString();

	}


	private String runCommand(String command) {
		logger.debug("runCommand shellId {} command {}", shellId, command);
		final Element bodyContent = DocumentHelper.createElement(QName.get("CommandLine", NS_WIN_SHELL));

		String encoded = command;
		encoded = "\"" + encoded + "\"";


		logger.info("Encoded command is {}", encoded);

		bodyContent.addElement(QName.get("Command", NS_WIN_SHELL)).addText(encoded);

		final Document requestDocument = getRequestDocument(Action.WS_COMMAND, ResourceURI.RESOURCE_URI_CMD, OptionSet.RUN_COMMAND, shellId, bodyContent);
		Document responseDocument = sendMessage(requestDocument, SoapAction.COMMAND_LINE);

		return getFirstElement(responseDocument, ResponseExtractor.COMMAND_ID);
	}


	private static String getFirstElement(Document doc, ResponseExtractor extractor) {
		@SuppressWarnings("unchecked")
        final List<Element> nodes = extractor.getXPath().selectNodes(doc);
		if (nodes.isEmpty())
			throw new RuntimeException("Cannot find " + extractor.getXPath() + " in " + toString(doc));

		final Element next = nodes.iterator().next();
		return next.getText();
	}

	private String openShell() {
		logger.debug("openShell");

		final Element bodyContent = DocumentHelper.createElement(QName.get("Shell", NS_WIN_SHELL));
		bodyContent.addElement(QName.get("InputStreams", NS_WIN_SHELL)).addText("stdin");
		bodyContent.addElement(QName.get("OutputStreams", NS_WIN_SHELL)).addText("stdout stderr");


		final Document requestDocument = getRequestDocument(Action.WS_ACTION, ResourceURI.RESOURCE_URI_CMD, OptionSet.OPEN_SHELL, null, bodyContent);
		Document responseDocument = sendMessage(requestDocument, SoapAction.SHELL);

		return getFirstElement(responseDocument, ResponseExtractor.SHELL_ID);

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

	private void addHeaders(SoapMessageBuilder.EnvelopeBuilder envelope, Action action, ResourceURI resourceURI, OptionSet optionSet, String shelId) throws URISyntaxException {
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

	public int getExitCode() {
		return Integer.parseInt(exitCode);
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

