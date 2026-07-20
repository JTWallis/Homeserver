package com.hybris.homeserver.endpoints.http.api.secret.user;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hybris.homeserver.endpoints.http.api.ErrorResponseDto;
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
		
		return ResponseEntity.ok(new ApiUserResponseDto(dto));
	}
	
	@GetMapping("/existing")
	public ResponseEntity<?> get(@RequestParam("username") Optional<String> username, HttpServletRequest request) {
		if(username.isEmpty() || username.get().isBlank()) {
			return ResponseEntity.ok(userService.getAll());
		}
		
		ApiUserResponseDto userDto = userService.getSingle(username.get());
		if(userDto == null) {
			return ResponseEntity.badRequest().body(new ErrorResponseDto(
					HttpStatus.BAD_REQUEST, request.getRequestURI(), "No such user '" + username.get() + "'"
			));
		}
		
		return ResponseEntity.ok(userDto);
	}
}
