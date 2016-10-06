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
package com.xebialabs.overthere.smb;

import com.xebialabs.overthere.cifs.PathMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SmbPaths {

    public static final char SLASH = '\\';
    public static final String EMPTY = "";

    static String getShareName(String sharePath) {
        int i = sharePath.indexOf(SLASH);
        if (i != -1)
            return sharePath.substring(0, i);
        return sharePath;
    }

    static String getSharePath(String hostPath, Map<String, String> pathMappings) {
        PathMapper mapper = new PathMapper(pathMappings);
        String p = mapper.toSharedPath(hostPath);
        int first = p.indexOf(SLASH);
        int last = p.lastIndexOf(SLASH);
        if (first == last && p.endsWith(String.valueOf(SLASH)))
            p = p.substring(0, p.length() - 1);
        return p;
    }

    static String getPathOnShare(String sharePath) {
        if (sharePath.endsWith(String.valueOf(SLASH)))
            sharePath = sharePath.substring(0, sharePath.length() - 1);
        int i = sharePath.indexOf(SLASH);
        if (i != -1)
            return sharePath.substring(i + 1, sharePath.length());
        else
            return EMPTY;
    }

    static String escapeForwardSlashes(String hostPath) {
        return hostPath.replace('/', SLASH);
    }

    static String getParentPath(String sharePath) {

        if (sharePath.endsWith(String.valueOf(SLASH)))
            sharePath = sharePath.substring(0, sharePath.length() - 1);

        int i = sharePath.lastIndexOf(SLASH);
        if (i != -1) {
            return sharePath.substring(0, i);
        }
        return null;
    }

    static String getFileName(String sharePath) {
        int i = sharePath.lastIndexOf(SLASH);
        if (i != -1)
            return sharePath.substring(i + 1);
        return sharePath;
    }

    static String join(String parent, String child) {
        return parent + SLASH + child;
    }

    static String[] getPathListFromOuterToInner(String sharePath) {
        String[] split = sharePath.split("\\\\");
        String path = EMPTY;
        List<String> l = new ArrayList<>();
        for (String s : split) {
            path = path + (path.isEmpty() ? EMPTY : SLASH) + s;
            l.add(path);
        }
        return l.toArray(new String[l.size()]);
    }
}
