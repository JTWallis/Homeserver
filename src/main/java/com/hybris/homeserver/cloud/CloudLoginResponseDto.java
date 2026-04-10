package com.hybris.homeserver.cloud;

public class CloudLoginResponseDto {
	private String username;
	
	public CloudLoginResponseDto(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
}
