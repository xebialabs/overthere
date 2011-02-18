package com.xebialabs.overthere;

@SuppressWarnings("serial")
public class AbortedException extends RuntimeException {

	public AbortedException() {
	    super();
    }

	public AbortedException(String message, Throwable cause) {
	    super(message, cause);
    }

	public AbortedException(String message) {
	    super(message);
    }

	public AbortedException(Throwable cause) {
	    super(cause);
    }

}
