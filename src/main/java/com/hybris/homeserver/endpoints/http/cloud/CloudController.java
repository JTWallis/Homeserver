package com.hybris.homeserver.endpoints.http.cloud;

import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cloud")
public class CloudController {
	private final CloudStorageService storageService;
	
	@Autowired
	public CloudController(CloudStorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping
	public String getFiles(HttpSession session, Model model) {
		// Load path from session.
		String currentPartialDir = (String) session.getAttribute(CloudAttribConstants.SESSION_PARTIAL_DIR);
		String navFilename = (String) session.getAttribute(CloudAttribConstants.SESSION_NAV_FILENAME);
		
		Path pathAbsolute = storageService.resolveLegalAbsolutePath(currentPartialDir, navFilename);
		String pathPartial = storageService.getPartialPath(pathAbsolute);
		
		// Collect all filenames and iconnames of the path.
		List<CloudFile> files = storageService.loadAsCloudFiles(pathAbsolute);
		
		// Store filenames, iconnames and current path in model for Thymeleaf.
		model.addAttribute(CloudAttribConstants.MODEL_FILES, files);
		model.addAttribute(CloudAttribConstants.MODEL_DIR, pathPartial);
		
		// Update current dir for upload and reset navigation for redirects from other controllers.
		session.setAttribute(CloudAttribConstants.SESSION_PARTIAL_DIR, pathPartial);
		session.setAttribute(CloudAttribConstants.SESSION_NAV_FILENAME, ".");
		
		return "cloud";
	}
	
	@PostMapping
	public String open(@RequestParam("partial_dir") String partialDir, @RequestParam("filename") String filename, HttpSession session) {
		// Store navigated path into session.
		session.setAttribute(CloudAttribConstants.SESSION_PARTIAL_DIR, partialDir);
		session.setAttribute(CloudAttribConstants.SESSION_NAV_FILENAME, filename);
		
		return "redirect:/cloud";
	}
	

}
