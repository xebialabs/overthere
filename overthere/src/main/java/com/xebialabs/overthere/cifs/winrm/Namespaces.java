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

