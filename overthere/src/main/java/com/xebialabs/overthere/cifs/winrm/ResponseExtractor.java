/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2011 XebiaLabs
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

import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.XPath;
import org.jaxen.SimpleNamespaceContext;

public enum ResponseExtractor {

	COMMAND_ID("CommandId"),
	EXIT_CODE("ExitCode"),
	SHELL_ID("Selector[@Name='ShellId']", Namespaces.NS_WSMAN_DMTF),
	STDOUT("Stream[@Name='stdout']"),
	STDERR("Stream[@Name='stderr']"),
	STREAM_DONE("CommandState[@State='http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Done']");

	private final String expr;
	private final Namespace ns;
	private final SimpleNamespaceContext namespaceContext;

	ResponseExtractor(String expr) {
		this(expr, Namespaces.NS_WIN_SHELL);
	}


	ResponseExtractor(String expr, Namespace ns) {
		this.expr = expr;
		this.ns = ns;
		namespaceContext = new SimpleNamespaceContext();
		namespaceContext.addNamespace(ns.getPrefix(), ns.getURI());
	}

	public XPath getXPath() {
		final XPath xPath = DocumentHelper.createXPath("//" + ns.getPrefix() + ":" + expr);
		xPath.setNamespaceContext(namespaceContext);
		return xPath;
	}
}

