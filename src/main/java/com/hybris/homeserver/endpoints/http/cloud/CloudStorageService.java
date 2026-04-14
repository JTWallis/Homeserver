package com.hybris.homeserver.endpoints.http.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hybris.homeserver.endpoints.http.cloud.download.FilenameAwareByteArrayResource;

@Service
public class CloudStorageService {

	public void store(String location, MultipartFile file) throws IOException, InvalidPathException, IllegalPathException {
		if(location == null || location.isBlank() || file.isEmpty()) {
			return;
		}
		
		Path path = Paths.get(location);
		
		Path destination = path.resolve(
				Paths.get(file.getOriginalFilename()))
				.normalize()
				.toAbsolutePath();
		
		// Security check destination path
		if(!isPathLegal(destination) || !destination.getParent().equals(path.toAbsolutePath())) {
			throw new IllegalPathException("Invalid path: " + destination.toString());
		}
		
		try(InputStream istream = file.getInputStream()) {
			Files.copy(istream, destination, StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	public Resource load(String filepath) throws InvalidPathException, IOException {
		Path path = Paths.get(filepath).normalize().toAbsolutePath();
		
		if(!isPathLegal(path)) {
			throw new IllegalPathException("Illegal path: " + path.toString());
		}
		
		return new FilenameAwareByteArrayResource(
				path.getFileName().toString(),
				Files.readAllBytes(path));
	}
	
	public Path getUserRoot() {
		return Paths.get("/");
	}
	
	public boolean isPathUserRoot(Path p) {
		return getUserRoot().equals(p);
	}
	
	public boolean isPathLegal(Path p)  {
		// TODO: Check for subdirectory of user-root
		return true;
	}
	
	public boolean isPathLegal(String p) {
		try {
			return isPathLegal(Paths.get(p).normalize());
		} catch(InvalidPathException e) {
			return false;
		}
	}

}
