package com.xebialabs.overthere.ssh;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.net.ServerSocket;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SshTunnelConnectionTest {

	@Test
	public void shouldNotHandOutDuplicatePortsWhenSocketsAlwaysSeemToBeFreeLikeOnWindows() {
		SshTunnelConnection.TunnelPortManager tunnelPortManager = new SshTunnelConnection.TunnelPortManager() {
			@Override
			protected ServerSocket tryBind(int localPort) {
				ServerSocket mock = mock(ServerSocket.class);
				when(mock.getLocalPort()).thenReturn(localPort);
				return mock;
			}
		};

		ServerSocket serverSocket = tunnelPortManager.leaseNewPort(1025);
		ServerSocket serverSocket2 = tunnelPortManager.leaseNewPort(1025);
		assertThat(serverSocket.getLocalPort(), Matchers.not(equalTo(serverSocket2.getLocalPort())));
		tunnelPortManager.returnPort(serverSocket);
		tunnelPortManager.returnPort(serverSocket2);
	}
}
