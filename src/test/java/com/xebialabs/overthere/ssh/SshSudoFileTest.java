package com.xebialabs.overthere.ssh;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;

public class SshSudoFileTest {

    public static final String REMOTE_PATH = "/tmp/file.txt";
    private SshSudoConnection connection;

    @BeforeClass
    public void setup() {
        connection = mock(SshSudoConnection.class);
    }

    @Test
    public void shouldNotBeEqualWhenOneIsTempAndAnotherIsNot() {
        SshSudoFile f1 = new SshSudoFile(connection, REMOTE_PATH, false);
        SshSudoFile f2 = new SshSudoFile(connection, REMOTE_PATH, true);
        assertThat(f1, is(not(f2)));
        assertThat(f1.hashCode(), is(not(f2.hashCode())));
    }

    @Test
    public void shouldBeEqual() {
        SshSudoFile f1 = new SshSudoFile(connection, REMOTE_PATH, false);
        SshSudoFile f2 = new SshSudoFile(connection, REMOTE_PATH, false);
        assertThat(f1, is(f2));
        assertThat(f1.hashCode(), is(f2.hashCode()));
    }

}
