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
package com.xebialabs.overthere.cifs;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xebialabs.overthere.RuntimeIOException;

import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PORT_DEFAULT;
import static java.lang.String.format;
import static java.util.regex.Pattern.quote;

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
        if (cifsPort != CIFS_PORT_DEFAULT) {
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

        // This '/' is *not* the Unix or the SMB URL separator. It's something that can appear when people use
        // Java-style Windows paths.
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
     * @param uncPath the UNC path to convert to a Windows file path
     * @return the Windows file path representing the UNC path
     */
    final String fromUncPath(String uncPath) {
        Matcher matcher = UNC_PATH_PATTERN.matcher(uncPath);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(format("UNC path '%s' did not match expected expression '%s'", uncPath, UNC_PATH_PATTERN));
        }
        return pathMapper.toLocalPath(matcher.group(1));
    }

    /**
     * Check whether the UNC path is valid.
     * @param uncPath the UNC path to check.
     * @return true if it is a valid UNC path.
     */
    final boolean isValidUncPath(String uncPath) {
        return UNC_PATH_PATTERN.matcher(uncPath).matches();
    }

}
