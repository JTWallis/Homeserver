package com.hybris.homeserver.endpoints.http.api.secret.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hybris.homeserver.endpoints.http.api.AuthResponseDto;

import jakarta.servlet.http.HttpServletRequest;


/**
 * Controller for authorizing to the /api/secret/** endpoint.
 * Certain people will be granted access to /api/secret/** sub-endpoint.
 * Each request to a secret api endpoint must include an auth token,
 * that is retrieved from here, by POSTing a correct username+password they were assigned.
 * 
 * A user is of either role ADMIN, or USER, with admins being able to access any secret endpoint.
 * Users will have an internal list of endpoints assigned, they are allowed to request to.
 */
@RestController
@RequestMapping("/api/secret/auth")
public class ApiAuthController {

	private static final Logger logger = LoggerFactory.getLogger(ApiAuthController.class);
	private final ApiAuthService authService;
	
	public ApiAuthController(ApiAuthService authService) {
		this.authService = authService;
	}
	
	@PostMapping
	public ResponseEntity<?> authenticate(@RequestBody ApiAuthRequestDto dto, HttpServletRequest request) {
		
		ResponseEntity<?> validation = ApiUserUtils.validateAuthRequestDto(dto, request.getRequestURI());
		if(validation.getStatusCode().isError()) {
			return validation;
		}
		
		AuthResponseDto response = authService.authenticate(dto);
		return ResponseEntity.ok(response);
	}

}
