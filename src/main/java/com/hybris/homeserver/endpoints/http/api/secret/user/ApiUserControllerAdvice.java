package com.hybris.homeserver.endpoints.http.api.secret.user;

import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hybris.homeserver.endpoints.http.api.ErrorResponseDto;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(assignableTypes = { ApiUserController.class })
@Order(0)
public class ApiUserControllerAdvice {

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponseDto> handleUsernameExisting(DataIntegrityViolationException e, HttpServletRequest request) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		return ResponseEntity.badRequest().body(new ErrorResponseDto(
				status,
				request.getRequestURI(),
				"Username already exists!"
		));
	}
	
}
