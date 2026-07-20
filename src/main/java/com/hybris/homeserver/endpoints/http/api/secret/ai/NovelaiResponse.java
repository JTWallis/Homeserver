package com.hybris.homeserver.endpoints.http.api.secret.ai;

public class NovelaiResponse {

	private int statusCode;
	private String message;
	
	public NovelaiResponse(int statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
	}
	
	public NovelaiResponse() {}

	public int getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}
}
