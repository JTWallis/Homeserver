package com.hybris.homeserver.endpoints.http.cloud.download;

import org.springframework.core.io.ByteArrayResource;

public class FilenameAwareByteArrayResource extends ByteArrayResource {
	
	private final String filename;

	public FilenameAwareByteArrayResource(String filename, byte[] byteArray, String description) {
		super(byteArray, description);
		this.filename = filename;
	}
	
	public FilenameAwareByteArrayResource(String filename, byte[] byteArray) {
		super(byteArray);
		this.filename = filename;
	}
	
	@Override
	public String getFilename() {
		return filename;
	}

}