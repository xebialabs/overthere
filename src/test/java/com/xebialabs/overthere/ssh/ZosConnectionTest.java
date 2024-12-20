package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;

import org.mockito.Mockito;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ZosConnectionTest {

    private SshConnection mockScpConnection;
    private SshConnection mockSftpConnection;
    private ZosConnection zosConnection;

    @BeforeClass
    public void setUp() {
        ConnectionOptions options = new ConnectionOptions();
        options.set(PORT, 22);
        options.set(OPERATING_SYSTEM, UNIX);
        options.set(ADDRESS, "host");
        options.set(USERNAME, "username");
        options.set(PASSWORD, "password");
        mockScpConnection = mock(SshConnection.class);
        mockSftpConnection = mock(SshConnection.class);

        zosConnection = Mockito.spy(new ZosConnection(options));
        doReturn(mockScpConnection).when(zosConnection).getConnectionForScp();
        doReturn(mockSftpConnection).when(zosConnection).getConnectionForSftp();

        doReturn(mockScpConnection).when(zosConnection).getConnection(SshConnectionType.SCP);
        doReturn(mockSftpConnection).when(zosConnection).getConnection(SshConnectionType.SFTP);
    }

    @Test
    public void testGetConnectionForScp() {
        assertThat(mockScpConnection, equalTo(zosConnection.getConnectionForScp()));
    }

    @Test
    public void testGetConnectionForSftp() {
        assertThat(mockSftpConnection, equalTo(zosConnection.getConnectionForSftp()));
    }

    @Test
    public void testGetConnection_Scp() {
        assertThat(zosConnection.getConnection(SshConnectionType.SCP), equalTo(mockScpConnection));
    }

    @Test
    public void testGetConnection_Sftp() {
        assertThat(zosConnection.getConnection(SshConnectionType.SFTP), equalTo(mockSftpConnection));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGetConnection_NullPointer() {
        zosConnection.getConnection(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetConnection_Unsupported() {
        zosConnection.getConnection(SshConnectionType.SFTP_CYGWIN);
    }


}