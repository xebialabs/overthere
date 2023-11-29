package com.xebialabs.overthere.ssh;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

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

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionOnPatternNull() {
        InputStream is = new ByteArrayInputStream(("echo UserName:").getBytes());
        new ReplacingInputStream(is, null, "ReplacePassword");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionOnInputStreamNull() throws IOException {
        ReplacingInputStream foo = new ReplacingInputStream(null, "SudoPassword", "ReplacePassword");
        foo.readAllBytes();
    }

    @Test
    public void shouldReplaceTextWithEmptyInInputStream() throws IOException {
        InputStream is = new ByteArrayInputStream(("echo SudoPassword:").getBytes());

        ReplacingInputStream foo = new ReplacingInputStream(is, "SudoPassword", "");
        String replaceStreamResult = new String(foo.readAllBytes());
        assertThat(replaceStreamResult, equalTo("echo :"));
    }

    @Test
    public void shouldReplaceTextWithLineFeedInInputStream() throws IOException {
        InputStream is = new ByteArrayInputStream(("echo \\n\\r SudoPassword:").getBytes());

        ReplacingInputStream foo = new ReplacingInputStream(is, "SudoPassword", "");
        String replaceStreamResult = new String(foo.readAllBytes());
        assertThat(replaceStreamResult, equalTo("echo \\n\\r :"));
    }

    @Test
    public void shouldExitFineWhenPatternNotFoundInByteArrayInputStream() throws IOException {
        InputStream is = new ByteArrayInputStream(("echo SudoPassword:").getBytes());

        ReplacingInputStream foo = new ReplacingInputStream(is, "notexisting", "");
        String replaceStreamResult = new String(foo.readAllBytes());
        assertThat(replaceStreamResult, equalTo("echo SudoPassword:"));
    }

    @Test(expectedExceptions = TimeoutException.class)
    public void shouldExitFineWhenPatternNotFoundInInputStream() throws IOException, TimeoutException {
        Process process = new ProcessBuilder("sleep", "100").start();
        InputStream is = process.getInputStream();
//        process.waitFor();

        ReplacingInputStream foo = new ReplacingInputStream(is, "notexisting", "replaced string");
        InputStreamReader isr = new TimedoutInputStreamReader(foo, 10 * 1000);
        StringBuilder sbr = new StringBuilder();
        try {
            for (;;) {
                int i = isr.read();
                if (i == -1) {
                    break;
                }
                sbr.append((char) i);
            }
        } catch (IOException ioe) {
            if (ioe.getMessage().contains("Input stream read timeout")) {
                throw new TimeoutException();
            }
        }

        String replaceStreamResult = sbr.toString();
        assertThat(replaceStreamResult, containsString("replaced string"));
    }
}

class TimedoutInputStreamReader extends InputStreamReader {

    private final int readTimeout;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    /**
     * TimedoutInputStreamReader
     *
     * @param in - input stream
     * @param readTimeout Zero or -ve timeout means indefinite wait else read will timeout after given milliseconds
     */
    public TimedoutInputStreamReader(InputStream in, int readTimeout) {
        super(in);
        this.readTimeout = readTimeout;
    }

    @Override
    public int read() throws IOException {
        if(readTimeout > 0) {
            return tryRead();
        } else {
            return super.read();
        }
    }

    private int tryRead() throws IOException {
        Future<Integer> future = executor.submit(() -> super.read());
        try {
            return future.get(readTimeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            System.out.println("Input stream read timeout");
            throw new IOException("Input stream read timeout");
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
}
