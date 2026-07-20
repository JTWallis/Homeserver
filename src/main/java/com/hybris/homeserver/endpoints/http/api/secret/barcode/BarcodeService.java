package com.hybris.homeserver.endpoints.http.api.secret.barcode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BarcodeService {

	private final String DIR_SCRIPT = "/home/homeserver/barcode-lookup/";
	private final String DIR_CODES = DIR_SCRIPT + "barcodes/";
	
	public BarcodeMetadata lookupMetadata(long code) throws IOException, InterruptedException, TimeoutException, ExecutionException {
		if(!hasCode(code)) {
			lookupBarcode(code);
		}
		
		Path pathMetadata = Paths.get(DIR_CODES, String.valueOf(code), "/metadata.json");
		
		// Lookup finished but no data for it exists
		if(!Files.exists(pathMetadata)) {
			return new BarcodeMetadata("", "");
		}
		
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(pathMetadata.toFile(), BarcodeMetadata.class);
	}
	
	public Resource lookupThumbnail(long code) throws IOException, InterruptedException, TimeoutException, ExecutionException {
		if(!hasCode(code)) {
			lookupBarcode(code);
		}
			
		Path pathThumbnail = Paths.get(DIR_CODES, String.valueOf(code), "/thumbnail.jpg");
		
		// Lookup finished but no data for it exists
		if (!Files.exists(pathThumbnail)) {
			return null;
		}
		
		byte[] bytes;
		bytes = Files.readAllBytes(pathThumbnail);
		return new ByteArrayResource(bytes);
	}
	
	private void lookupBarcode(long code) throws IOException, InterruptedException, TimeoutException, ExecutionException {
		// Barcode lookup happens via python script with code as param
		ProcessBuilder pb = new ProcessBuilder(
				"python3",
				DIR_SCRIPT + "lookup.py",
				String.valueOf(code)
		);
		
		pb.directory(new File(DIR_SCRIPT));
		Process process = pb.start();
		
		// Tomcat gives new thread for each HTTP request, so blocking should be fine here.
		boolean finished = process.waitFor(30, TimeUnit.SECONDS);
		if(!finished) {
			process.destroyForcibly();
			throw new TimeoutException("Barcode-Lookup script timed out.");
		}
	}

	private boolean hasCode(long code) {
		Path path = Paths.get(DIR_CODES, String.valueOf(code));
		return Files.exists(path);
	}
	
}
