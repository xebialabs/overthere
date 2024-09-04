package com.xebialabs.overthere;


import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.BaseCifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL;

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

		testfileCopyinTargetWinowsMachine(options);
		testfileCopyinTargetWinowsMachineNoShare(options);
		testfileTransmitFromlocaltoTargetWindowsMachine(options);
	}

	private static void testfileCopyinTargetWinowsMachine(ConnectionOptions targetOptions) {
		OverthereConnection connection = Overthere.getConnection("smb", targetOptions);
		try {
			OverthereFile from = connection.getFile("C:\\foltest\\arulshare\\makeLocalCpy.log");
			OverthereFile to = connection.getFile("C:\\foltest\\arulshare\\makeLocalCpy2.log");
			from.copyTo(to);
		} finally {
			connection.close();
		}
	}

	private static void testfileCopyinTargetWinowsMachineNoShare(ConnectionOptions targetOptions) {
		OverthereConnection connection = Overthere.getConnection("smb", targetOptions);
		try {
			OverthereFile from = connection.getFile("C:\\arulnoshare\\makenoshare.log");
			OverthereFile to = connection.getFile("C:\\arulnoshare\\makenoshareCopy2.log");
			from.copyTo(to);
		} finally {
			connection.close();
		}
	}

	private static void testfileTransmitFromlocaltoTargetWindowsMachine(ConnectionOptions targetOptions) {
		ConnectionOptions optionsLocal = new ConnectionOptions();
		OverthereConnection connLocal = Overthere.getConnection("local", optionsLocal);
		OverthereConnection connection = Overthere.getConnection("smb", targetOptions);
		try {
			OverthereFile from = connLocal.getFile("/Users/arulprasad/projects/2024/TestResults/make.log");
			OverthereFile to = connection.getFile("C:\\foltest\\arulshare\\makeLocalCpy2.log");
			from.copyTo(to);
		} finally {
			connection.close();
		}
	}

}
