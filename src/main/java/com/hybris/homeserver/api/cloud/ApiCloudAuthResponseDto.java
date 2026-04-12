package com.hybris.homeserver.api.cloud;

import com.hybris.homeserver.api.AuthResponseDto;

public class ApiCloudAuthResponseDto extends AuthResponseDto {
	private String username;
	
	public ApiCloudAuthResponseDto(String token, String username) {
		super(token);
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
}
