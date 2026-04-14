package com.hybris.homeserver.endpoints.http.cloud.download;

import java.io.IOException;
import java.nio.file.InvalidPathException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hybris.homeserver.endpoints.http.api.ErrorResponseDto;
import com.hybris.homeserver.endpoints.http.cloud.CloudStorageService;
import com.hybris.homeserver.endpoints.http.cloud.IllegalPathException;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CloudFileDownloadController {
	
	private final CloudStorageService storageService;
	
	@Autowired
	public CloudFileDownloadController(CloudStorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/cloud/download")
	public ResponseEntity<?> download(@RequestParam("path") String filepath, HttpServletRequest request) {
		if(filepath == null || filepath.isBlank()) {
			System.out.println("Download filepath null");
		}
		
		Resource resource;
		long contentLength;
		
		try {
			resource = storageService.load(filepath);
			contentLength = resource.contentLength();
		} catch(InvalidPathException | IllegalPathException e) {
			return ResponseEntity.notFound().build();
		} catch(IOException e) {
			return ResponseEntity.badRequest().body(new ErrorResponseDto(
					HttpStatus.BAD_REQUEST,
					request.getRequestURI(), 
					"Error when reading file!"
			));
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename());
		
		return ResponseEntity.ok()
				.headers(headers)
				.contentLength(contentLength)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
	}
}
