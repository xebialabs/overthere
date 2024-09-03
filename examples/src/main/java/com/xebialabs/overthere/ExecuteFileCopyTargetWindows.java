package com.xebialabs.overthere;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.BaseCifsConnectionBuilder.PATH_SHARE_MAPPINGS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_NATIVE;

import com.google.common.collect.ImmutableMap;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;

public class ExecuteFileCopyTargetWindows {

	public static void main(String[] args) {

		ConnectionOptions options = new ConnectionOptions();
		options.set(ADDRESS, "172.18.60.118");
		options.set(USERNAME, "Administrator");
		options.set(PASSWORD, "devopsqe@123");
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, WINRM_INTERNAL	);
		//options.set(FILE_COPY_COMMAND_FOR_WINDOWS, "copy {0} {1}");
		//options.set(PATH_SHARE_MAPPINGS, ImmutableMap.of("c:\\foltest\\arulshare", "foltest\\arulshare"))	;
		//ConnectionOptions optionsLocal = new ConnectionOptions();
		//OverthereConnection connLocal = Overthere.getConnection("local", optionsLocal);

		OverthereConnection connection = Overthere.getConnection("smb", options);


		try {
			//	connection.execute(CmdLine.build("type", "\\windows\\system32\\drivers\\etc\\hosts"));
			//	connection.execute(CmdLine.build("ipconfig", "/all"));
			OverthereFile from = connection.getFile("C:\\foltest\\arulshare\\makeLocalCpy.log");
			OverthereFile to = connection.getFile("C:\\foltest\\arulshare\\makeLocalCpy2.log");
			from.copyTo(to);
		} finally {
			connection.close();
		}
	}

}
