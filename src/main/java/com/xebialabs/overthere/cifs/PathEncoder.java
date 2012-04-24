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

package com.xebialabs.overthere.cifs;

import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_CIFS_PORT;
import static java.lang.String.format;
import static java.util.regex.Pattern.quote;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xebialabs.overthere.RuntimeIOException;

/**
 * Conversions to/from UNC, SMB and Windows file paths
 */
class PathEncoder {
	// "\\host-name\share" or "\\host-name\share\" or "\\host-name\share\path"
	private static final Pattern UNC_PATH_PATTERN = Pattern.compile("\\\\\\\\[^\\\\]+\\\\([^\\\\]+(?:\\\\.*)?)");
	private static final char WINDOWS_SEPARATOR = '\\';
	private static final char SMB_URL_SEPARATOR = '/';

	private final String smbUrlPrefix;
	private final PathMapper pathMapper;

	PathEncoder(String username, String password, String address, int cifsPort, Map<String, String> pathMappings) {
		StringBuilder urlPrefix = new StringBuilder();
		urlPrefix.append("smb://");
		// this is *not* the Windows file separator ;-)
		if (username != null) {
			urlPrefix.append(urlEncode(username.replaceFirst(quote("\\"), ";")));
			urlPrefix.append(":");
			urlPrefix.append(urlEncode(password));
			urlPrefix.append("@");
		}
		urlPrefix.append(urlEncode(address));
		if (cifsPort != DEFAULT_CIFS_PORT) {
			urlPrefix.append(":");
			urlPrefix.append(cifsPort);
		}
		urlPrefix.append(SMB_URL_SEPARATOR);
		this.smbUrlPrefix = urlPrefix.toString();
		this.pathMapper = new PathMapper(pathMappings);
	}

	private static String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeIOException("Unable to construct SMB URL", e);
		}
	}

	String toSmbUrl(String hostPath) {
		if (hostPath.length() < 2) {
			throw new IllegalArgumentException(format("Host path '%s' is too short", hostPath));
		}

		if (hostPath.charAt(1) != ':') {
			throw new IllegalArgumentException(format("Host path '%s' does not have a colon (:) as its second character", hostPath));
		}

		// This '/' is *not* the Unix or the SMB URL separator. It's something that can appear when people use Java-style Windows paths.
		hostPath = hostPath.replace('/', WINDOWS_SEPARATOR);

		if (hostPath.length() >= 3) {
			if (hostPath.charAt(2) != WINDOWS_SEPARATOR) {
				throw new IllegalArgumentException(format("Host path '%s' does not have a backslash (\\) as its third character", hostPath));
			}
		}

		StringBuilder smbUrl = new StringBuilder(smbUrlPrefix);
		smbUrl.append(pathMapper.toSharedPath(hostPath).replace(WINDOWS_SEPARATOR, SMB_URL_SEPARATOR));
		return smbUrl.toString();
	}

	/**
	 * @param uncPath
	 *            the UNC path to convert to a Windows file path
	 * @return the Windows file path representing the UNC path
	 */
	final String fromUncPath(String uncPath) {
		Matcher matcher = UNC_PATH_PATTERN.matcher(uncPath);
		if (!matcher.matches()) {
			throw new IllegalArgumentException(format("UNC path '%s' did not match expected expression '%s'", uncPath, UNC_PATH_PATTERN));
		}
		return pathMapper.toLocalPath(matcher.group(1));
	}

}
