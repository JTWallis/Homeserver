package com.hybris.homeserver.endpoints.http.cloud;

public class IllegalPathException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IllegalPathException(String message) {
		super(message);
	}
	
	public IllegalPathException(String message, Throwable cause) {
		super(message, cause);
	}
}
