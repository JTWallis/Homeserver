package com.hybris.homeserver.endpoints.http.api.secret.ai;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hybris.homeserver.endpoints.http.api.ErrorResponseDto;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/secret/ai")
public class NovelaiController {
	private final NovelaiService novelaiService;
	
	public NovelaiController(NovelaiService novelaiService) {
		this.novelaiService = novelaiService;
	}
	
	@PostMapping("/question")
	public ResponseEntity<?> promptQuestion(@RequestBody NovelaiPromptDto promptDto, HttpServletRequest request) {
		ResponseEntity<?> validation = validateUserPrompt(promptDto, request.getRequestURI());
		if(validation.getStatusCode().value() != HttpStatus.OK.value()) {
			return validation;
		}
		
		NovelaiResponse response = novelaiService.promptGlm(promptDto);
		return buildResponseEntity(response, request.getRequestURI());
	}
	
	@PostMapping("/story")
	public ResponseEntity<?> promptStory(@RequestBody NovelaiPromptDto promptDto, HttpServletRequest request) {
		ResponseEntity<?> validation = validateUserPrompt(promptDto, request.getRequestURI());
		if(validation.getStatusCode().value() != HttpStatus.OK.value()) {
			return validation;
		}
		
		NovelaiResponse response = novelaiService.promptKayra(promptDto);
		return buildResponseEntity(response, request.getRequestURI());
	}
	
	private ResponseEntity<?> validateUserPrompt(NovelaiPromptDto promptDto, String requestUri) {
		if(promptDto == null) {
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, requestUri, "Prompt DTO cannot be null!"));
		}		
		
		if(promptDto.getPrompt() == null || promptDto.getPrompt().isBlank()) {
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, requestUri, "Prompt cannot be empty!"));
		}
		
		return ResponseEntity.ok("");
	}
	
	private ResponseEntity<?> buildResponseEntity(NovelaiResponse response, String requestUri) {
		if(response == null || response.getMessage() == null) {
			return ResponseEntity
					.badRequest()
					.body(new ErrorResponseDto(
							HttpStatus.BAD_REQUEST,
							requestUri,
							"Error when handling prompt: No response."
					));
		}
		
		HttpStatusCode code = HttpStatusCode.valueOf(response.getStatusCode());
		if(code.isError()) {
			return ResponseEntity
					.status(code)
					.body(new ErrorResponseDto(
							HttpStatus.valueOf(response.getStatusCode()),
							requestUri,
							response.getMessage()
					));
		}
		
		return ResponseEntity.ok().body(response);
	}
	
}
