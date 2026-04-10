package com.hybris.homeserver;

import java.time.Instant;

import org.springframework.http.HttpStatus;

public class ErrorResponseDto {

	private Instant timestamp;
	private int status;
	private String error;
	private String message;
	
	public ErrorResponseDto(HttpStatus httpStatus, String customMessage) {
		this.timestamp = Instant.now();
		this.status = httpStatus.value();
		this.error = httpStatus.getReasonPhrase();
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
	
	public String getMessage() {
		return message;
	}
	

}