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

import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;
import org.dom4j.XPath;
import org.jaxen.SimpleNamespaceContext;

enum ResponseExtractor {

    COMMAND_ID("CommandId"),
    EXIT_CODE("CommandState[@State='http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Done']/rsp:ExitCode", Namespaces.NS_WIN_SHELL),
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
