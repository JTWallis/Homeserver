package com.hybris.homeserver.endpoints.http.cloud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hybris.homeserver.endpoints.http.api.ErrorResponseDto;

public class CloudLoginUtils {
	
	private final static String REGEX_ALLOWED_CHARS_USERNAME = "[a-zA-Z0-9#_\\-\\+]+";

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
		
		
		if(!login.getUsername().matches(REGEX_ALLOWED_CHARS_USERNAME)) {
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(
							HttpStatus.NOT_ACCEPTABLE,
							requestUri,
							"Username can only contain characters from a-z, digits and special characters _ - + #"
			));
		}
		
		
		return ResponseEntity.ok("");
	}
}
