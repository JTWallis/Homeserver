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
import org.springframework.core.io.Resource;
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
	
	public List<CloudFile> loadAsCloudFiles(String filepath) {
		Path path = getLegalPath(filepath);
		return buildFilenames(path);
	}
	
	public Resource loadAsResource(String filepath) throws InvalidPathException, IOException, FileSizeLimitExceededException {
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
		
		// Insert parent-navigation as first entry, only if in subdir of user-root.
		if(!isPathUserRoot(p)) {
			files.add(0, new CloudFile("..", ICON_NAME_FOLDER));
		}
		
		return files;
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
		return Paths.get("/");
	}
	
	public boolean isPathUserRoot(Path p) {
		return getUserRoot().equals(p);
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
		// TODO: Check for subdirectory of user-root
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
