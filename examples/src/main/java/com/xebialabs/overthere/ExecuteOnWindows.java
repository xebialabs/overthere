package com.xebialabs.overthere;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;

public class ExecuteOnWindows {

	public static void main(String[] args) {

		ConnectionOptions options = new ConnectionOptions();
		options.set(ADDRESS, "windows-box");
		options.set(USERNAME, "Administrator");
		options.set(PASSWORD, "secret");
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, TELNET);
		OverthereConnection connection = Overthere.getConnection("cifs", options);

		try {
			connection.execute(CmdLine.build("type", "\\windows\\system32\\drivers\\etc\\hosts"));
		} finally {
			connection.close();
		}
	}

}
