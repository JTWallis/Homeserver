package com.hybris.homeserver.database.cloud;

public class CloudFile {

	private String filename;
	private String iconname;

	public CloudFile(String filename, String iconname) {
		this.filename = filename;
		this.iconname = iconname;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getIconname() {
		return iconname;
	}
}
