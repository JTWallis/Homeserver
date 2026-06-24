package com.hybris.homeserver.endpoints.http.api.barcode;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hybris.homeserver.endpoints.http.api.ErrorResponseDto;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class BarcodeController {

	private final BarcodeService barcodeService;
	
	public BarcodeController(BarcodeService barcodeService) {
		this.barcodeService = barcodeService;
	}
	
	@GetMapping("/api/barcode/metadata/{code}")
	public ResponseEntity<?> lookupMetadata(@PathVariable("code") long code, HttpServletRequest request) throws IOException, InterruptedException, TimeoutException, ExecutionException {
		ResponseEntity<?> validated = validateCode(code, request);
		if(validated.getStatusCode() != HttpStatus.OK) {
			return validated;
		}
		
		BarcodeMetadata metadata = barcodeService.lookupMetadata(code);
		
		return ResponseEntity
				.ok(metadata
		);
	}
	
	@GetMapping("/api/barcode/thumbnail/{code}")
	public ResponseEntity<?> lookupThumbnail(@PathVariable("code") long code, HttpServletRequest request) throws IOException, InterruptedException, TimeoutException, ExecutionException, NullPointerException {
		ResponseEntity<?> validated = validateCode(code, request);
		if(validated.getStatusCode() != HttpStatus.OK) {
			return validated;
		}

		Resource resource = barcodeService.lookupThumbnail(code);
		
		if(resource == null) {
			return ResponseEntity
					.badRequest()
					.body(new ErrorResponseDto(
						HttpStatus.NOT_FOUND,
						request.getRequestURI(),
						"No thumbnail for barcode " + code
			));
		}
		
		long contentLength = resource.contentLength();

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=thumbnail.jpg");
		
		return ResponseEntity.ok()
				.headers(headers)
				.contentLength(contentLength)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
	}
	
	private ResponseEntity<?> validateCode(long code, HttpServletRequest request) {
		if(code <= 0) {
			return ResponseEntity
					.badRequest()
					.body(new ErrorResponseDto(
						HttpStatus.BAD_REQUEST,
						request.getRequestURI(),
						"Barcode must be positive"
			));
		}
		
		return ResponseEntity.ok().build();
	}
}
