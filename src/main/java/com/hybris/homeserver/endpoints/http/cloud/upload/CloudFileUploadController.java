package com.hybris.homeserver.endpoints.http.cloud.upload;

import java.io.IOException;
import java.nio.file.InvalidPathException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hybris.homeserver.endpoints.http.cloud.CloudAttribConstants;
import com.hybris.homeserver.endpoints.http.cloud.CloudStorageService;
import com.hybris.homeserver.endpoints.http.cloud.IllegalPathException;

import jakarta.servlet.http.HttpSession;

@Controller
public class CloudFileUploadController {
	private final CloudStorageService storageService;
	
	@Autowired
	public CloudFileUploadController(CloudStorageService storageService) {
		this.storageService = storageService;
	}
	
	@PostMapping("/cloud/upload")
	public String uploadFile(@RequestParam("file") MultipartFile file,  RedirectAttributes redirectAttributes, HttpSession httpSession) {
		String path = (String) httpSession.getAttribute(CloudAttribConstants.SESSION_PARTIAL_DIR);
		
		try {
			storageService.store(path, file);
		} catch(IllegalPathException | InvalidPathException e) {
			redirectAttributes.addFlashAttribute("uploadErrorMsg", "Invalid path!");
		} catch(IOException e) {
			redirectAttributes.addFlashAttribute("uploadErrorMsg", "Error on path validation!");
		}
		
		return "redirect:/cloud";
	}
	
}
