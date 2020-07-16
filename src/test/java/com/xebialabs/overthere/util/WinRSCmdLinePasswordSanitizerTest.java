package com.xebialabs.overthere.util;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class WinRSCmdLinePasswordSanitizerTest {

    final String CARET_ESCAPE = "|<>&^\r";

    @Test
    public void shouldEscapeAmpersandsInACommandLineArgument() {
        String arg = "Password+-&";
        assertThat("Password+-^&", equalTo(WinRSCommandLinePasswordSanitizer.sanitize(arg)));
    }

    @Test
    public void shouldQuoteArgumentIfItContainsWhiteSpaces(){
        String arg = "hello+-& world";
        assertThat("\"hello+-& world\"", equalTo(WinRSCommandLinePasswordSanitizer.sanitize(arg)));
    }

    @Test
    public void shouldEscapeAllInstancesOfAmpersandFromCommandLineArg() {
        String arg = "Password&&+-&";
        assertThat("Password^&^&+-^&", equalTo(WinRSCommandLinePasswordSanitizer.sanitize(arg)));
    }

    @Test
    public void shouldReplaceAllCaratEscapeCharsWithCaratInACommandLineArg() {
        String arg = "Password";
        for (char c : CARET_ESCAPE.toCharArray()) {
            assertThat("Password^" + c, equalTo(WinRSCommandLinePasswordSanitizer.sanitize(arg + c)));
        }
    }

    @Test
    public void shouldEscapeAllCharactersIfPasswordContainsOnlySpecialChars(){
        String arg = "^&+-&";
        assertThat("^^^&+-^&", equalTo(WinRSCommandLinePasswordSanitizer.sanitize(arg)));
    }

    @Test
    public void shouldNotEscapeSingleQuote(){
        String arg = "a'b";
        assertThat("a'b", equalTo(WinRSCommandLinePasswordSanitizer.sanitize(arg)));
    }

    @Test
    public void shouldReturnBlankStringAsIs(){
        String arg = "";
        assertThat("", equalTo(WinRSCommandLinePasswordSanitizer.sanitize(arg)));
    }

    @Test
    public void shouldEscapeCarriageReturnWithCaret(){
        String arg = "Pass\rword";
        assertThat("Pass^\rword", equalTo(WinRSCommandLinePasswordSanitizer.sanitize(arg)));
    }

    @Test
    public void shouldNotEscapePercentage(){
        String arg = "Password%";
        assertThat("Password%", equalTo(WinRSCommandLinePasswordSanitizer.sanitize(arg)));
    }
}
