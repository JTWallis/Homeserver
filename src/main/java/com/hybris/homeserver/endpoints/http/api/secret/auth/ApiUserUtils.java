package com.hybris.homeserver.endpoints.http.api.secret.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hybris.homeserver.endpoints.http.api.ErrorResponseDto;
import com.hybris.homeserver.endpoints.http.api.secret.user.ApiUserDto;

public class ApiUserUtils {

	private final static String REGEX_ALLOWED_CHARS_USERNAME = "[a-zA-Z0-9#_\\-\\+]+";
	
	public static ResponseEntity<?> validateAuthRequestDto(ApiAuthRequestDto dto, String requestUri) {
		if(dto == null) {
			return buildNotAcceptableResponse("Request body cannot be null!", requestUri);
		}
		
		if(dto.getUsername() == null || dto.getUsername().isBlank()) {
			return buildNotAcceptableResponse("Username cannot be empty!", requestUri);
		}
		
		if(dto.getPassword() == null || dto.getPassword().isBlank()) {
			return buildNotAcceptableResponse("Password cannot be empty!", requestUri);
		}
		
		if(!dto.getUsername().matches(REGEX_ALLOWED_CHARS_USERNAME)) {
			return buildNotAcceptableResponse(
					"Username can only contain characters from a-z, digits and special characters _ - + #",
					requestUri
			);
		}
		
		return ResponseEntity.ok().build();
	}
	
	public static ResponseEntity<?> validateApiUserDto(ApiUserDto dto, String requestUri) {
		ResponseEntity<?> validateAuth = validateAuthRequestDto(dto, requestUri);
		if(validateAuth.getStatusCode().isError()) return validateAuth;
		
		final String role = dto.getRole();
		if(role == null || role.isBlank()) {
			return buildNotAcceptableResponse("Role cannot be empty!", requestUri);
		}
		
		if(!(role.equals("USER") || role.equals("ADMIN"))) {
			return buildNotAcceptableResponse("Role must be either USER or ADMIN", requestUri);
		}
		
		if(dto.getEndpoints() == null) {
			return buildNotAcceptableResponse("Endpoints cannot be null!", requestUri);
		}
		
		return ResponseEntity.ok().build();
	}
	
	private static ResponseEntity<ErrorResponseDto> buildNotAcceptableResponse(String message, String requestUri) {
		return ResponseEntity
				.status(HttpStatus.NOT_ACCEPTABLE)
				.body(new ErrorResponseDto(
						HttpStatus.NOT_ACCEPTABLE,
						requestUri,
						message
		));
	}
}
