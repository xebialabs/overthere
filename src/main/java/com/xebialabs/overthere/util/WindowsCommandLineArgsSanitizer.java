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

    private static final char[] SPECIAL_CHARS = " |<>&^\r\n".toCharArray();

    private static final String WHITE_SPACE = " ";

    private static final Character ESCAPE_CHAR = '^';

    /**
     * Percent is treated in another way, so it cannot be escaped by ^ char, but with `%` instead.
     * @see <a href="https://www.robvanderwoude.com/escapechars.php">Escape Characters.</a>
     */
    private static final Character PERCENT_CHAR = '%';

    public static String sanitize(String str) {
        if (str.contains(WHITE_SPACE)) {
            return "\"" + str + "\"";
        }
        return  escapeSpecialCharacters(str);
    }

    private static String escapeSpecialCharacters(String str) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < str.length(); j++) {
            char c = str.charAt(j);
            if (c == PERCENT_CHAR) {
                builder.append(PERCENT_CHAR);
            }
            if (contains(c, SPECIAL_CHARS)) {
                builder.append(ESCAPE_CHAR);
            }
            builder.append(c);
        }
        return builder.toString();
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
        if (str.indexOf(PERCENT_CHAR) >= 0) {
            return true;
        }
        for (char c : SPECIAL_CHARS) {
            if (str.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }

}
