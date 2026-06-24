package com.hybris.homeserver.endpoints.http;

import java.io.IOException;
import java.nio.file.InvalidPathException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.hybris.homeserver.endpoints.http.api.ErrorResponseDto;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(TypeMismatchException.class)
	public ResponseEntity<ErrorResponseDto> handleTypeMismatch(TypeMismatchException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.BAD_REQUEST, "Invalid path param type", request);
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponseDto> handleIO(AccessDeniedException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", request);
	}
	
	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<ErrorResponseDto> handleNullPointer(NullPointerException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", request);
	}
	
	@ExceptionHandler(NoResourceFoundException.class)
	public String handleResourceNotFound(NoResourceFoundException ex, HttpServletRequest request) {
		return "no_page";
	}
	
	@ExceptionHandler(InvalidPathException.class)
	public ResponseEntity<ErrorResponseDto> handleInvalidPath(InvalidPathException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.NOT_FOUND, "Path not found", request);
	}
	
	@ExceptionHandler(IOException.class)
	public ResponseEntity<ErrorResponseDto> handleIO(IOException ex, HttpServletRequest request) {
		return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Could not get resource", request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDto> handleEx(Exception ex, HttpServletRequest request) {
		logger.error(ex.toString());
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", request);
	}

	private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
		return ResponseEntity
				.status(status)
				.body(new ErrorResponseDto(
						status,
						request.getRequestURI(),
						message)
				);
	}
}
