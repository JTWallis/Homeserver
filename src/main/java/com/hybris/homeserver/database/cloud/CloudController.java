package com.hybris.homeserver.database.cloud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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
	
	private final String ICON_NAME_FOLDER 	= "icon_folder.ico";
	private final String ICON_NAME_IMG		= "icon_img.ico";
	private final String ICON_NAME_TXT		= "icon_txt.ico";
	private final String ICON_NAME_OTHER	= "icon_other.ico";

	@GetMapping
	public String getFiles(HttpSession session, Model model) {
		// Load path from session.
		String path = (String) session.getAttribute("path");
		
		Path p;
		
		try {
			if(path == null || path.isBlank() || !isPathLegal(path)) {
				p = Paths.get(getDefaultPath()).toRealPath();
			} else {
				p = Paths.get(path).toRealPath();
			}
		} catch(IOException | InvalidPathException e) {
			p = Paths.get(getDefaultPath()).toAbsolutePath();
		}

		// Collect all filenames and iconnames of the path.
		List<CloudFile> files = buildFilenames(p);
		
		// Store filenames, iconnames and current path in model for Thymeleaf.
		model.addAttribute("files", files);
		model.addAttribute("dir", p.toAbsolutePath().toString());
		
		return "cloud";
	}
	
	@PostMapping
	public String open(@RequestParam("path") String dir, HttpSession session) {
		String path;
		try {
			Path p = Paths.get(dir).toRealPath();
			path = isPathLegal(p)
					? p.toString() 
					: getDefaultPath();
		} catch(IOException | InvalidPathException e) {
			path = getDefaultPath();
		}
		
		// Store navigated path into session.
		session.setAttribute("path", path);
		
		return "redirect:/cloud";
	}
	
	private List<CloudFile> buildFilenames(Path p) {
		List<CloudFile> files;
		try {
			// Collect all names and icon-types of the files directly in this path.
			// First entry is always the path itself, so skip it.
			files = Files.walk(p, 1)
					.skip(1)
					.map(f -> new CloudFile(f.getFileName().toString(), getFileIconName(f)))
					.collect(Collectors.toList());
		} catch (IOException e) {
			files = List.of();
		}
		
		// Insert parent-navigation as first entry, only if in subdir of user-root.
		if(!isPathUserRoot(p)) {
			files.add(0, new CloudFile("..", ICON_NAME_FOLDER));
		}
		
		return files;
	}
	
	private boolean isPathUserRoot(Path p) {
		// TODO: Replace with user-root dir
		Path userRoot = Paths.get("/");
		
		return p.equals(userRoot);
	}
	
	private boolean isPathLegal(Path p) {
		// TODO: Check for existing path
		// TODO: Check for subdirectory of user-root
		return true;
	}
	
	private boolean isPathLegal(String p) {
		try {
			return isPathLegal(Paths.get(p).toRealPath());
		} catch(IOException | InvalidPathException e) {
			return false;
		}
	}
	
	private String getFileIconName(Path path) {
		if(Files.isDirectory(path)) {
			return ICON_NAME_FOLDER;
		}
		
		String fileType;
		try {
			String filename = path.getFileName().toString();
			fileType = filename.substring(filename.lastIndexOf('.') + 1);
		} catch(IndexOutOfBoundsException e) {
			return ICON_NAME_OTHER;
		}
		
		switch(fileType.toLowerCase()) {
			case "png":
			case "jpg":
			case "jpeg":
				return ICON_NAME_IMG;
			case "txt":
				return ICON_NAME_TXT;
			default:
				return ICON_NAME_OTHER;
		}
	}
	
	private String getDefaultPath() {
		// TODO: Replace with user-root dir
		return "/home";
	}
}
