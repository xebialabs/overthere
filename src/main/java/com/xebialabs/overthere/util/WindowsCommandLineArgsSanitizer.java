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
package com.xebialabs.overthere.util;

public class WindowsCommandLineArgsSanitizer {

    public static final String WHITE_SPACE = " ";

    private static final char[] CARET_ESCAPE = new char[]{'|', '<', '>', '&', '^', '\n', '\r'};

    private static final char[] SLASH_ESCAPE = new char[]{'\"'};

    public static String sanitize(String str) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < str.length(); j++) {
            char c = str.charAt(j);
            builder.append(getEscapeString(c));
            builder.append(c);
        }
        return builder.toString();
    }

    private static String getEscapeString(char str) {
        if (contains(str, CARET_ESCAPE)) {
            return "^";
        } else if (contains(str, SLASH_ESCAPE)) {
            return "\\";
        }
        return "";
    }

    private static boolean contains(char c, char[] chars) {
        for (char caret_char : chars) {
            if (caret_char == c) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAnySpecialChars(String str) {
        return containsAnySpecialCharacter(str, SLASH_ESCAPE) || containsAnySpecialCharacter(str, CARET_ESCAPE) || str.contains(WHITE_SPACE);
    }

    private static boolean containsAnySpecialCharacter(String str,char[] chars) {
        for (char c : chars) {
            if (str.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }
}
