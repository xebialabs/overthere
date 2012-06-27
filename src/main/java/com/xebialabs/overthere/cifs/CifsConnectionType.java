package com.xebialabs.overthere.cifs;

/**
 * Enumeration of CIFS connection types.
 */
public enum CifsConnectionType {
	
	/**
	 * A CIFS connection that uses Telnet to execute commands, to a Windows host.
	 */
	TELNET,
	
	/**
	 * A CIFS connection that uses WinRM over HTTP to execute commands, to a Windows host.
	 */
	WINRM_HTTP,
	
	/**
	 * A CIFS connection that uses WinRM over HTTPS to execute commands, to a Windows host.
	 */
	WINRM_HTTPS,

	/**
	 * A CIFS Connection that uses WinRM over HTTP with Kerberos Authentication to execute commands, to a windows
	 * host.
	 */
	WINRM_HTTP_KB5,

	/**
	 * A CIFS Connection that uses WinRM over HTTPS with Kerberos Authentication to execute commands, to a windows host.
	 */
	WINRM_HTTPS_KB5,

}
