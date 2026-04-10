package com.hybris.homeserver.cloud;

public class CloudLoginDto {

	private String username;
	private String password;
	
	public CloudLoginDto() {}
	
	public CloudLoginDto(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
}
