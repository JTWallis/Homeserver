package com.hybris.homeserver.endpoints.http.api;

import java.time.Instant;

import org.springframework.http.HttpStatus;

public class ErrorResponseDto {

	private Instant timestamp;
	private int status;
	private String error;
	private String path;
	private String message;
	
	public ErrorResponseDto(HttpStatus httpStatus,  String path, String customMessage) {
		this.timestamp = Instant.now();
		this.status = httpStatus.value();
		this.error = httpStatus.getReasonPhrase();
		this.path = path;
		this.message = customMessage;
	}
	
	public Long getTimestamp() {
		return timestamp.toEpochMilli();
	}
	
	public int getStatus() {
		return status;
	}
	
	public String getError() {
		return error;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getMessage() {
		return message;
	}

}