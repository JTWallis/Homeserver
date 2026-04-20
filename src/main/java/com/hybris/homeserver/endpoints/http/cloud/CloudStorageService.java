package com.hybris.homeserver.endpoints.http.cloud;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hybris.homeserver.endpoints.http.cloud.download.FilenameAwareByteArrayResource;

@Service
public class CloudStorageService {

	private final long MAX_DOWNLOAD_SIZE = 128 * 1024 * 1024;	// 128 MB.
	private final String ICON_NAME_FOLDER 	= "icon_folder.ico";
	private final String ICON_NAME_IMG		= "icon_img.ico";
	private final String ICON_NAME_TXT		= "icon_txt.ico";
	private final String ICON_NAME_OTHER	= "icon_other.ico";
	private final String DIR_USER_ROOTS		= "/home/.users/";
	
	private static final Logger logger = LoggerFactory.getLogger(CloudStorageService.class);
	
	public List<CloudFile> loadAsCloudFiles(Path absolutePath) {
		return buildFilenames(absolutePath);
	}
	
	public Resource loadAsResource(String filepath, String filename) throws InvalidPathException, IOException, FileSizeLimitExceededException {
		Path path = resolveLegalAbsolutePath(filepath, filename);

		if(!isPathLegal(path)) {
			throw new IllegalPathException("Illegal path: " + path.toString());
		}

		String fileName = path.getFileName().toString();
		byte[] fileBytes;
		
		if(Files.isDirectory(path)) {
			fileName += ".zip";
			fileBytes = pack(path);
		} else {
			if(!isDownloadFileSizeLegal(Files.size(path))) {
				throw new FileSizeLimitExceededException(filepath, -1, MAX_DOWNLOAD_SIZE);
			}
			
			fileBytes = Files.readAllBytes(path);
		}
		
		return new FilenameAwareByteArrayResource(fileName, fileBytes);	
	}
	
	public void store(String partialLocation, MultipartFile file) throws IOException, InvalidPathException, IllegalPathException {
		if(partialLocation == null || partialLocation.isBlank() || file.isEmpty()) {
			return;
		}
		
		// Build absolute path from partial dir.
		//   On ROLE_USER remove leading "/".
		Path dir = (isUserAdmin())
				? Path.of(partialLocation)
				: getUserRoot().resolve(partialLocation.substring(1));
		
		// Technically unnecessary security check but can never be unsure.
		if(!isPathLegal(dir)) {
			throw new IllegalPathException("Invalid path: " + dir.toString());
		}
		
		Path destination = dir.resolve(file.getOriginalFilename());
		
		try(InputStream istream = file.getInputStream()) {
			Files.copy(istream, destination, StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	public void createUserRoot(String username) {
		try {
			Files.createDirectory(Path.of(DIR_USER_ROOTS + username));
		} catch (IOException e) {
			logger.warn("Could not create Cloud user root dir for user '" + username + "' with exception: " + e.getMessage());
		}
	}
	
	private List<CloudFile> buildFilenames(Path p) {
		List<CloudFile> files;
		try {
			// Collect all names and icon-types of the files directly in this path.
			// First entry is always the path itself, so skip it.
			files = Files.walk(p, 1)
					.skip(1)
					.map(f -> new CloudFile(f.getFileName().toString(), getFileIconName(f)))
					.sorted((cf1, cf2) -> {
						// Faster to sort by dir via checking Strings rather
						//   than calling Files.isDirectory() due to no IO overhead.
						boolean cf1Dir = cf1.getIconname().equals(ICON_NAME_FOLDER);
						boolean cf2Dir = cf2.getIconname().equals(ICON_NAME_FOLDER);
						if( cf1Dir && !cf2Dir) return -1;
						if(!cf1Dir &&  cf2Dir) return 1;
						return cf1.getFilename().toString().toLowerCase()
								.compareTo(cf2.getFilename().toString().toLowerCase());
					})
					.collect(Collectors.toList());
		} catch (IOException e) {
			files = List.of();
		}
		
		// Insert parent-navigation as first entry, only if either
		//   a) User is     an admin and is below the real root directory
		//   b) User is not an admin and is below the user root directory
		boolean isUserAdmin = isUserAdmin();
		if( (isUserAdmin && !p.equals(Paths.get("/")) ) ||
			(!isUserAdmin && !isPathUserRoot(p)) ) 
		{
			files.add(0, new CloudFile("..", ICON_NAME_FOLDER));
		}
		
		return files;
	}
	
	private byte[] pack(Path folderPath) throws FileSizeLimitExceededException, IOException {
		byte[] result;
		long bytesSum = 0;
		
		try (ByteArrayOutputStream bstream = new ByteArrayOutputStream()) {
			try (ZipOutputStream zstream = new ZipOutputStream(bstream)) {
				
				Iterator<Path> iter = 
						Files.walk(folderPath)
						.filter(path -> !Files.isDirectory(path))
						.iterator();
				
				while(iter.hasNext()) {
					Path path = iter.next();
					
					// Check folder size here, to not walk recursively twice.
					bytesSum += Files.size(path);
					if(!isDownloadFileSizeLegal(bytesSum)) {
						throw new FileSizeLimitExceededException(folderPath.toString(), -1, MAX_DOWNLOAD_SIZE);
					}
					
					ZipEntry zipEntry = new ZipEntry(folderPath.relativize(path).toString());
					zstream.putNextEntry(zipEntry);
					Files.copy(path, zstream);
					zstream.closeEntry();
				}
			}
			result = bstream.toByteArray();
		}
		return result;
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
	
	private boolean isDownloadFileSizeLegal(long bytes) {
		return bytes <= MAX_DOWNLOAD_SIZE;
	}
	
	public Path getUserRoot() {
		String username = SecurityContextHolder.getContext()
				.getAuthentication()
				.getName();
		
		if(username == null || username.isBlank()) {
			throw new UsernameNotFoundException("Could not find username in getUserRoot!");
		}
		
		return Paths.get(DIR_USER_ROOTS + username);
	}
	
	private boolean isUserAdmin() {
		return SecurityContextHolder
				.getContext()
				.getAuthentication()
				.getAuthorities()
				.stream()
				.anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
	}
	
	public boolean isPathUserRoot(Path p) {
		return getUserRoot().equals(p);
	}
	
	public Path resolveLegalAbsolutePath(String currentPartialDir, String navFilename) {
		Path userRoot = getUserRoot();
		
		if(currentPartialDir == null || currentPartialDir.isBlank()) {
			return userRoot;
		}
		
		if(navFilename == null || navFilename.isBlank()) {
			navFilename = ".";
		}
		
		boolean isAdmin = isUserAdmin();
		Path fullPath;
		Path currentDirAbs;
		
		// Make partial dir an absolute path and resolve with with the navigated file.
		try {
			currentDirAbs = (isAdmin)
					? Path.of(currentPartialDir)
					: Path.of(userRoot.toString(), currentPartialDir);
			
			// Navigating into a file can never have another entry than ".." anyway.
			if(!Files.isDirectory(currentDirAbs)) {
				return currentDirAbs.getParent();
			}
			
			// Resolve navigated path without special names like ".."
			fullPath = currentDirAbs.resolve(navFilename).toRealPath();
		} catch(InvalidPathException | IOException e) {
			return userRoot;
		}

		// Security check if ROLE_USER tried navigating outside of user root.
		if(!isAdmin && !fullPath.startsWith(userRoot)) {
			return userRoot;
		}
		
		return fullPath;
	}
	
	public String getPartialPath(Path absolutePath) {
		if(isUserAdmin()) {
			return absolutePath.toString();
		}
		
		Path userRoot = getUserRoot();
		int userRootCount = userRoot.getNameCount();
		int absPathCount = absolutePath.getNameCount();
		
		// Security check if ROLE_USER tried navigating outside of user root,
		//   or subpath could not work since path is already user root.
		if( (!absolutePath.startsWith(userRoot) ) || 
				(absPathCount <= userRootCount) ) 
		{
			return "/";
		}

		// Get subpath with leading "/" and without username in path name.
		//   E.g. "/.users/UserA/TestFolder" => "/TestFolder"
		return "/" + absolutePath.subpath(userRoot.getNameCount(), absolutePath.getNameCount()).toString();
	}
	
	public Path getLegalPath(String path) {
		if(path == null || path.isBlank()) {
			return getUserRoot();
		}
		
		Path p;
		
		try {
			p = Paths.get(path).normalize();
		} catch(InvalidPathException e) {
			return getUserRoot();
		}
		
		if(!isPathLegal(p)) {
			return getUserRoot();
		}
		
		return p;
	}
	
	public boolean isPathLegal(Path p)  {
		try {
			p = p.toRealPath();
		} catch(IOException e) {
			return false;
		}
		
		Path userRoot = getUserRoot();
		
		if(!isUserAdmin() && !p.startsWith(userRoot)) {
			return false;
		}
		
		return true;
	}
	
	public boolean isPathLegal(String p) {
		if(p == null || p.isBlank()) {
			return false;
		}
		
		try {
			return isPathLegal(Paths.get(p).normalize());
		} catch(InvalidPathException e) {
			return false;
		}
	}

}
