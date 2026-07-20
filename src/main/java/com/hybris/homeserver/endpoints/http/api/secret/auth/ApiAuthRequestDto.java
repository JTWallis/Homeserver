package com.hybris.homeserver.endpoints.http.api.secret.auth;


public class ApiAuthRequestDto {

	private String username;
	private String password;
	
	public ApiAuthRequestDto(String endpointRequest, String username, String password) {
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
