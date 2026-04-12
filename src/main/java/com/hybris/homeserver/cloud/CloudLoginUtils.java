package com.hybris.homeserver.cloud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hybris.homeserver.ErrorResponseDto;

public class CloudLoginUtils {

	public static ResponseEntity<?> validateLoginObject(CloudLoginDto login, String requestUri) {
		if(login == null) {
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, requestUri, "Login DTO cannot be null!"));
		}
		
		if(login.getUsername() == null || login.getUsername().isBlank()) {
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, requestUri, "Username cannot be empty!"));
		}
		
		if(login.getPassword() == null || login.getPassword().isBlank()) {
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, requestUri, "Password cannot be empty!"));
		}
		
		return ResponseEntity.ok("");
	}
}
