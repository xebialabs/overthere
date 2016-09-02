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

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static java.lang.String.format;
import static java.util.regex.Pattern.quote;

public class PathMapper {
    private static final String DRIVE_DESIGNATOR = ":";
    private static final String ADMIN_SHARE_DESIGNATOR = "$";
    private static final Pattern ADMIN_SHARE_PATTERN = Pattern.compile("[a-zA-Z]" + quote(ADMIN_SHARE_DESIGNATOR));

    private final SortedMap<String, String> sharesForPaths;
    private final Map<String, String> pathsForShares;

    public PathMapper(final Map<String, String> mappings) {
        // longest first, so reverse lexicographical order
        SortedMap<String, String> sharesForPath = new TreeMap<String, String>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareTo(o1);
            }
        });
        Map<String, String> pathsForShare = new HashMap<String, String>();

        for (Entry<String, String> mapping : mappings.entrySet()) {
            String pathPrefixToMatch = mapping.getKey();
            String shareForPathPrefix = mapping.getValue();
            checkArgument(sharesForPath.put(pathPrefixToMatch.toLowerCase(), shareForPathPrefix) == null, "path prefix is not unique in mapping %s -> %s", pathPrefixToMatch, shareForPathPrefix);
            checkArgument(pathsForShare.put(shareForPathPrefix.toLowerCase(), pathPrefixToMatch) == null, "share is not unique in mapping %s -> %s", shareForPathPrefix, pathPrefixToMatch);
        }
        this.sharesForPaths = Collections.unmodifiableSortedMap(sharesForPath);
        this.pathsForShares = Collections.unmodifiableMap(pathsForShare);
    }

    /**
     * Attempts to use provided path-to-share mappings to convert the given local path to a remotely accessible path,
     * using the longest matching prefix if available.
     * <p/>
     * Falls back to using administrative shares if none of the explicit mappings applies to the path to convert.
     *
     * @param path the local path to convert
     * @return the remotely accessible path (using shares) at which the local path can be accessed using SMB
     */
    public String toSharedPath(String path) {
        final String lowerCasePath = path.toLowerCase();
        // assumes correct format drive: or drive:\path
        String mappedPathPrefix = null;
        for (String s : sharesForPaths.keySet()) {
            if (lowerCasePath.startsWith(s)) {
                mappedPathPrefix = s;
                break;
            }
        }
        // the share + the remainder of the path if found, otherwise the path with ':' replaced by '$'
        return ((mappedPathPrefix != null) ? sharesForPaths.get(mappedPathPrefix) + path.substring(mappedPathPrefix.length()) : path.substring(0, 1)
                + ADMIN_SHARE_DESIGNATOR
                + path.substring(2));
    }

    /**
     * @param path the remotely accessible path to convert (minus the host name, i.e. beginning with the share)
     * @return the local path (using drive letters) corresponding to the path that is remotely accessible using SMB
     */
    public String toLocalPath(String path) {
        final String lowerCasePath = path.toLowerCase();
        // assumes correct format share or share\path
        String mappedShare = null;
        for (String s : pathsForShares.keySet()) {
            if (lowerCasePath.startsWith(s)) {
                mappedShare = s;
                break;
            }
        }

        if (mappedShare != null) {
            return pathsForShares.get(mappedShare) + path.substring(mappedShare.length());
        } else if ((path.length() >= 2) && ADMIN_SHARE_PATTERN.matcher(path.substring(0, 2)).matches()) {
            return path.substring(0, 1) + DRIVE_DESIGNATOR + path.substring(2);
        } else {
            throw new IllegalArgumentException(format("Remote path name '%s' uses unrecognized (i.e. neither mapped nor administrative) share", path));
        }
    }
}