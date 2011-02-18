package com.xebialabs.overthere;

@SuppressWarnings("serial")
public class RuntimeIOException extends RuntimeException {

	public RuntimeIOException() {
	    super();
    }

	public RuntimeIOException(String message, Throwable cause) {
	    super(message, cause);
    }

	public RuntimeIOException(String message) {
	    super(message);
    }

	public RuntimeIOException(Throwable cause) {
	    super(cause);
    }

}
