package com.hybris.homeserver.endpoints.http.api.cloud;

import com.hybris.homeserver.endpoints.http.api.AuthResponseDto;

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
