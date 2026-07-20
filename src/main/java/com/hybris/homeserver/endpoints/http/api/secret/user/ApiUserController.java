package com.hybris.homeserver.endpoints.http.api.secret.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hybris.homeserver.endpoints.http.api.secret.auth.ApiUserUtils;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/secret/user")
public class ApiUserController {

	private final ApiUserService userService;
	
	public ApiUserController(ApiUserService userService) {
		this.userService = userService;
	}
	
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody ApiUserDto dto, HttpServletRequest request) {
		ResponseEntity<?> validation = ApiUserUtils.validateApiUserDto(dto, request.getRequestURI());
		if(validation.getStatusCode().isError()) {
			return validation;
		}

		userService.registerUser(dto);
		
		return ResponseEntity.noContent().build();
	}
	
}
