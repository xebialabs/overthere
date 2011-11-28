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

import com.google.common.collect.Lists;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

import java.util.List;

/**
 */
public enum OptionSet {

	OPEN_SHELL(Lists.newArrayList(new KeyValuePair("WINRS_NOPROFILE", "FALSE"), new KeyValuePair("WINRS_CODEPAGE", "437"))),
	RUN_COMMAND(Lists.newArrayList(new KeyValuePair("WINRS_CONSOLEMODE_STDIN", "TRUE")));

	private final List<KeyValuePair> keyValuePairs;


	OptionSet(List<KeyValuePair> keyValuePairs) {
		this.keyValuePairs = keyValuePairs;
	}

	public Element getElement() {
		final Element optionSet = DocumentHelper.createElement(QName.get("OptionSet", Namespaces.NS_WSMAN_DMTF));
		for (KeyValuePair p : keyValuePairs) {
			optionSet.addElement(QName.get("Option", Namespaces.NS_WSMAN_DMTF)).addAttribute("Name", p.getKey()).addText(p.getValue());
		}
		return optionSet;
	}
}

