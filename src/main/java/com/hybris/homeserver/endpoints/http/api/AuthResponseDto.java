package com.hybris.homeserver.endpoints.http.api;

public class AuthResponseDto {
	private String token;
	
	public AuthResponseDto(String token) {
		this.token = token;
	}
	
	public String getToken() {
		return token;
	}
}
