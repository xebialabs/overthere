package com.xebialabs.overthere.ssh;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;

public class ReplacingInputStreamTest {

    @Test
    public void shouldReplaceTextInInputStream() throws IOException {
        InputStream is = new ByteArrayInputStream(("echo SudoPassword:").getBytes());

        ReplacingInputStream foo = new ReplacingInputStream(is, "SudoPassword", "ReplacePassword");
        String replaceStreamResult = new String(foo.readAllBytes());
        assertThat(replaceStreamResult, containsString("ReplacePassword"));
    }

    @Test
    public void shouldReplaceTextInMultipleOccurrence() throws IOException {
        InputStream is = new ByteArrayInputStream(("Warning: don't share your SudoPassword\r\n" +
                "echo SudoPassword:").getBytes());
        ReplacingInputStream foo = new ReplacingInputStream(is, "SudoPassword", "ReplacePassword");
        String replaceStreamResult = new String(foo.readAllBytes());

        Pattern pattern = Pattern.compile("ReplacePassword");
        Matcher match = pattern.matcher(replaceStreamResult);
        int counter = 0;
        while (match.find()) {
            counter++;
        }
        assertThat(counter, equalTo(2));
    }

    @Test
    public void shouldReplacementCaseSensitiveInInputStream() throws IOException {
        InputStream is = new ByteArrayInputStream(("echo abc:").getBytes());

        ReplacingInputStream foo = new ReplacingInputStream(is, "Abc", "ReplacePassword");
        String replaceStreamResult = new String(foo.readAllBytes());
        assertThat(replaceStreamResult, not(containsString("ReplacePassword")));
    }

    @Test
    public void shouldNotReplaceIfMatchNotFound() throws IOException {
        InputStream is = new ByteArrayInputStream(("echo UserName:").getBytes());

        ReplacingInputStream foo = new ReplacingInputStream(is, "Password", "ReplacePassword");
        String replaceStreamResult = new String(foo.readAllBytes());
        assertThat(replaceStreamResult, not(containsString("ReplacePassword")));
    }
}
