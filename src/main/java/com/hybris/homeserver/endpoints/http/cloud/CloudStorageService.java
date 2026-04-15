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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hybris.homeserver.endpoints.http.cloud.download.FilenameAwareByteArrayResource;

@Service
public class CloudStorageService {

	private final long MAX_DOWNLOAD_SIZE = 128 * 1024 * 1024;	// 128 MB.
	
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
	
	public Resource load(String filepath) throws InvalidPathException, IOException, FileSizeLimitExceededException {
		Path path = Paths.get(filepath).normalize().toAbsolutePath();
		
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
	
	private boolean isDownloadFileSizeLegal(long bytes) {
		return bytes <= MAX_DOWNLOAD_SIZE;
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
