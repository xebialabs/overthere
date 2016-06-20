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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WindowsCmdLineArgsSanitizerTest {

    final String CARET_ESCAPE = "|<>&^\r";

    final String SLASH_ESCAPE = "\"";

    @Test
    public void shouldEscapeAmpersandsInACommandLineArgument() {
        String arg = "Password+-&";
        assertEquals("Password+-^&", WindowsCommandLineArgsSanitizer.sanitizeWinrsCommandLineArgs(arg));
    }

    @Test
    public void shouldEscapeAllInstancesOfAmpersandFromCommandLineArg() {
        String arg = "Password&&+-&";
        assertEquals("Password^&^&+-^&", WindowsCommandLineArgsSanitizer.sanitizeWinrsCommandLineArgs(arg));
    }

    @Test
    public void shouldReplaceAllCaratEscapeCharsWithCaratInACommandLineArg() {
        String arg = "Password";
        for (char c : CARET_ESCAPE.toCharArray()) {
            assertEquals("Password^" + c, WindowsCommandLineArgsSanitizer.sanitizeWinrsCommandLineArgs(arg + c));
        }
    }

    @Test
    public void shouldReplaceAllCaratEscapeCharsWithSlashInACommandLineArg() {
        String arg = "Password";
        for (char c : SLASH_ESCAPE.toCharArray()) {
            assertEquals("Password\\" + c, WindowsCommandLineArgsSanitizer.sanitizeWinrsCommandLineArgs(arg + c));
        }
    }

    @Test
    public void shouldEscapeAllCharactersIfPasswordContainsOnlySpecialChars(){
        String arg = "^&+-&";
        assertEquals("^^^&+-^&", WindowsCommandLineArgsSanitizer.sanitizeWinrsCommandLineArgs(arg));
    }

    @Test
    public void shouldNotEscapeSingleQuote(){
        String arg = "a'b";
        assertEquals("a'b", WindowsCommandLineArgsSanitizer.sanitizeWinrsCommandLineArgs(arg));
    }

    @Test
    public void shouldReturnBlankStringAsIs(){
        String arg = "";
        assertEquals("", WindowsCommandLineArgsSanitizer.sanitizeWinrsCommandLineArgs(arg));
    }

    @Test
    public void shouldEscapeCarriageReturnWithCaret(){
        String arg = "Pass\rword";
        assertEquals("Pass^\rword", WindowsCommandLineArgsSanitizer.sanitizeWinrsCommandLineArgs(arg));
    }
}
