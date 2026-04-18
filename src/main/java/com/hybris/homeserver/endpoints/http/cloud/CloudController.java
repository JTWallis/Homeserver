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
		String currentPartialDir = (String) session.getAttribute("path");
		String navFilename = (String) session.getAttribute("nav_filename");
		
		Path pathAbsolute = storageService.resolveLegalAbsolutePath(currentPartialDir, navFilename);
		String pathPartial = storageService.getPartialPath(pathAbsolute);
		
		// Collect all filenames and iconnames of the path.
		List<CloudFile> files = storageService.loadAsCloudFiles(pathAbsolute);
		
		// Store filenames, iconnames and current path in model for Thymeleaf.
		model.addAttribute("files", files);
		model.addAttribute("dir", pathPartial);
		
		return "cloud";
	}
	
	@PostMapping
	public String open(@RequestParam("path") String dir, @RequestParam("filename") String filename, HttpSession session) {
		// Store navigated path into session.
		session.setAttribute("path", dir);
		session.setAttribute("nav_filename", filename);
		
		return "redirect:/cloud";
	}
	

}
