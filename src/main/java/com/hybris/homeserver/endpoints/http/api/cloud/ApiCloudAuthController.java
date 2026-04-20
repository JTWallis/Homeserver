package com.hybris.homeserver.endpoints.http.api.cloud;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hybris.homeserver.database.cloud.CloudLoginEntity;
import com.hybris.homeserver.database.cloud.CloudLoginService;
import com.hybris.homeserver.endpoints.http.api.ErrorResponseDto;
import com.hybris.homeserver.endpoints.http.cloud.CloudLoginDto;
import com.hybris.homeserver.endpoints.http.cloud.CloudLoginUtils;
import com.hybris.homeserver.endpoints.http.cloud.CloudStorageService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/api/cloud/auth")
public class ApiCloudAuthController {
	private static final Logger logger = LoggerFactory.getLogger(ApiCloudAuthController.class);
	private final CloudLoginService loginService;
	
	public ApiCloudAuthController(CloudLoginService loginService) {
		this.loginService = loginService;
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> authenticate(@RequestBody CloudLoginDto login, HttpServletRequest request) {
		
		ResponseEntity<?> validation = CloudLoginUtils.validateLoginObject(login, request.getRequestURI());
		if(validation.getStatusCode().value() != HttpStatus.OK.value()) {
			return validation;
		}
		
		try {
			ApiCloudAuthResponseDto response = loginService.authenticate(login);
			return ResponseEntity.ok(response);
		
		} catch(AuthenticationException e) {
			logger.info("Could not authenticate user '" + login.getUsername() + "' with Exception: " + e.getMessage());
			return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(new ErrorResponseDto(
						HttpStatus.UNAUTHORIZED,
						request.getRequestURI(),
						"Could not authenticate user " + login.getUsername()
				));
		}
	}
	
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody CloudLoginDto login, CloudStorageService storageService, HttpServletRequest request) {
		ResponseEntity<?> validation = CloudLoginUtils.validateLoginObject(login, request.getRequestURI());
		if(validation.getStatusCode().value() != HttpStatus.OK.value()) {
			return validation;
		}
		
		// Persist entity.
		CloudLoginEntity entity;
		try {
			entity = loginService.addUser(login);
		} catch(DataIntegrityViolationException e) {
			logger.info("Tried to create already existing user '" + login.getUsername() + "'");
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, request.getRequestURI(), "Username already exists!"));
		}
		
		if(entity == null) {
			logger.warn("Returned entity null for user '" + login.getUsername() + "'");
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, request.getRequestURI(), "Entity is null!"));
		}
		
		// Create user root directory.
		storageService.createUserRoot(entity.getUsername());
		
		ApiCloudAuthResponseDto responseDto = loginService.authenticate(login);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(responseDto);
	}
}
