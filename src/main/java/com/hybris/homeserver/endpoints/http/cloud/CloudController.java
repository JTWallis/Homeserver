package com.hybris.homeserver.endpoints.http.cloud;

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
		String path = (String) session.getAttribute("path");

		// Collect all filenames and iconnames of the path.
		List<CloudFile> files = storageService.loadAsCloudFiles(path);
		
		// Store filenames, iconnames and current path in model for Thymeleaf.
		model.addAttribute("files", files);
		model.addAttribute("dir", storageService.getLegalPath(path).toString());
		
		return "cloud";
	}
	
	@PostMapping
	public String open(@RequestParam("path") String dir, HttpSession session) {
		// Store navigated path into session.
		session.setAttribute("path", storageService.getLegalPath(dir).toString());
		
		return "redirect:/cloud";
	}
	

}
