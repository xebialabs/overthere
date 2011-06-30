/*
 * This file is part of WinRM.
 *
 * WinRM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WinRM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WinRM.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere.winrm;

import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.XPath;
import org.jaxen.SimpleNamespaceContext;

/**
 */
public enum ResponseExtractor {

	COMMAND_ID("CommandId"),
	EXIT_CODE("ExitCode"),
	SHELL_ID("Selector[@Name='ShellId']", WinRMURI.NS_WSMAN_DMTF),
	STDOUT("Stream[@Name='stdout']"),
	STDERR("Stream[@Name='stderr']"),
	STREAM_DONE("CommandState[@State='http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Done']");

	private final String expr;
	private final Namespace ns;
	private final SimpleNamespaceContext namespaceContext;

	ResponseExtractor(String expr) {
		this(expr, WinRMURI.NS_WIN_SHELL);
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
