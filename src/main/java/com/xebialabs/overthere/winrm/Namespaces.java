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

import org.dom4j.Namespace;

/**
 * Contains namespaces used by WinRM.
 */
public class Namespaces {

    public static final Namespace NS_SOAP_ENV = Namespace.get("env", "http://www.w3.org/2003/05/soap-envelope");
    public static final Namespace NS_ADDRESSING = Namespace.get("a", "http://schemas.xmlsoap.org/ws/2004/08/addressing");
    public static final Namespace NS_CIMBINDING = Namespace.get("b", "http://schemas.dmtf.org/wbem/wsman/1/cimbinding.xsd");
    public static final Namespace NS_ENUM = Namespace.get("n", "http://schemas.xmlsoap.org/ws/2004/09/enumeration");
    public static final Namespace NS_TRANSFER = Namespace.get("x", "http://schemas.xmlsoap.org/ws/2004/09/transfer");
    public static final Namespace NS_WSMAN_DMTF = Namespace.get("w", "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd");
    public static final Namespace NS_WSMAN_MSFT = Namespace.get("p", "http://schemas.microsoft.com/wbem/wsman/1/wsman.xsd");
    public static final Namespace NS_SCHEMA_INST = Namespace.get("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    public static final Namespace NS_WIN_SHELL = Namespace.get("rsp", "http://schemas.microsoft.com/wbem/wsman/1/windows/shell");
    public static final Namespace NS_WSMAN_FAULT = Namespace.get("f", "http://schemas.microsoft.com/wbem/wsman/1/wsmanfault");

}
