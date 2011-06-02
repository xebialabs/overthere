package com.xebialabs.overthere;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CmdLineArgument implements Serializable {

	private String arg;

	private boolean isPassword;

	private CmdLineArgument(String arg, boolean password) {
		this.arg = arg;
		isPassword = password;
	}

	public static CmdLineArgument arg(String arg) {
		checkNotNull(arg, "Cannot create null argument");
		return new CmdLineArgument(arg, false);
	}

	public static CmdLineArgument password(String arg) {
		checkNotNull(arg, "Cannot create null password argument");
		return new CmdLineArgument(arg, true);
	}

	public boolean isPassword() {
		return isPassword;
	}

	public String getArg() {
		return arg;
	}

	public String toString(boolean forLogging) {
		if (isPassword() && forLogging) {
			return "********";
		}
		return arg;
	}

	/**
	 * Will hide the password by default.
	 */
	@Override
	public String toString() {
		return toString(true);
	}

}
