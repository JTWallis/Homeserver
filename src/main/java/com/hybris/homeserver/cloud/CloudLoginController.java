package com.hybris.homeserver.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hybris.homeserver.ErrorResponseDto;
import com.hybris.homeserver.database.cloud.CloudLoginEntity;
import com.hybris.homeserver.database.cloud.CloudLoginService;

@Controller
@RequestMapping("/api/cloud/login")
public class CloudLoginController {

	private static final Logger logger = LoggerFactory.getLogger(CloudLoginController.class);
	private final CloudLoginService loginService;
	
	public CloudLoginController(CloudLoginService loginService) {
		this.loginService = loginService;
	}
	
	@PostMapping
	public ResponseEntity<?> addUser(@RequestBody CloudLoginDto login) {
		ResponseEntity<?> validation = validateLoginObject(login);
		if(validation.getStatusCode().value() != HttpStatus.OK.value()) {
			return validation;
		}
		
		CloudLoginEntity entity;
		try {
			entity = loginService.addUser(login);
		} catch(DataIntegrityViolationException e) {
			logger.info("Tried to create already existing user '" + login.getUsername() + "'");
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, "Username already exists!"));
		}
		
		if(entity == null) {
			logger.warn("Returned entity null for user '" + login.getUsername() + "' and password '" + login.getPassword() + "'");
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, "Entity is null!"));
		}
		
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(new CloudLoginResponseDto(entity.getUsername()));
	}
	
	
	@GetMapping
	public ResponseEntity<?> isLoginValid(@RequestBody CloudLoginDto login) {
		ResponseEntity<?> validation = validateLoginObject(login);
		if(validation.getStatusCode().value() != HttpStatus.OK.value()) {
			return validation;
		}
		
		try {
			boolean valid = loginService.isLoginValid(login.getUsername(), login.getPassword());
			
			if(!valid) {
				logger.info("Tried to log into user '" + login.getUsername() + "' with invalid password '" + login.getPassword() + "'");
				
				return ResponseEntity
						.status(HttpStatus.NOT_ACCEPTABLE)
						.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, "Invalid login!"));
			}
			
			return ResponseEntity
					.noContent().build();
		
		} catch(EmptyResultDataAccessException e) {
			logger.info("Tried to log into non-existing user '" + login.getUsername() + "'");
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, "Username does not exist! (TODO: Identical error messages for more security)"));
		}
	}
	
	private ResponseEntity<?> validateLoginObject(CloudLoginDto login) {
		if(login == null) {
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, "Login DTO cannot be null!"));
		}
		
		if(login.getUsername() == null || login.getUsername().isBlank()) {
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, "Username cannot be empty!"));
		}
		
		if(login.getPassword() == null || login.getPassword().isBlank()) {
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, "Password cannot be empty!"));
		}
		
		return ResponseEntity.ok("");
	}
}
