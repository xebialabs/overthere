package com.xebialabs.overthere.ssh;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class SshScpFileTest {

    private SshScpConnection connection;
    private SshScpFile sshScpFile;

    @BeforeClass
    public void setup() {
        connection = mock(SshScpConnection.class);
        sshScpFile = new SshScpFile(connection, "/foo/bar");
    }

    @Test
    public void shouldParseDirectoryWithAclOnLs() {
        SshScpFile.LsResults results = new SshScpFile.LsResults();
        boolean b = sshScpFile.parseLsOutputLine(results, "drwxr-xr-x+ 10 ajvanerp  staff    340 Dec 17 15:28 build");
        assertThat("Should be a directory", results.isDirectory);
        assertThat("Should be executable", results.canExecute);
    }

    @Test
    public void shouldParseDirectoryWithMacOSExtendedAttrsOnLs() {
        SshScpFile.LsResults results = new SshScpFile.LsResults();
        boolean b = sshScpFile.parseLsOutputLine(results, "drwxr-xr-x@ 10 ajvanerp  staff    340 Dec 17 15:28 build");
        assertThat("Should be a directory", results.isDirectory);
        assertThat("Should be executable", results.canExecute);
    }
}
