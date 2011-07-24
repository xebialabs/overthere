package com.xebialabs.overthere.cifs;

/**
 * Enumeration of CIFS connection types.
 */
public enum CifsConnectionType {
	
	/**
	 * A CIFS connection that uses Telnet to execute commands.
	 */
	TELNET,
	
	/**
	 * A CIFS connection that uses WinRM over HTTP to execute commands.
	 */
	WINRM_HTTP,
	
	/**
	 * A CIFS connection that uses WinRM over HTTPS to execute commands.
	 */
	WINRM_HTTPS

}
