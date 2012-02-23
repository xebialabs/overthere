package com.xebialabs.overthere.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class Sockets {
	public static boolean checkAvailable(int localPort) {
		ServerSocket ss = null;
		try {
			ss = getServerSocket(localPort);
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException ignored) {
					// Ok
				}
			}
		}
	}

	public static ServerSocket getServerSocket(int port) throws IOException {
		ServerSocket ss = new ServerSocket();
		ss.setReuseAddress(true);
		ss.bind(new InetSocketAddress("localhost", port));
		return ss;
	}

}
