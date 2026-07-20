package com.hybris.homeserver.endpoints.http.api.secret.user;

import com.hybris.homeserver.endpoints.http.api.secret.auth.ApiAuthRequestDto;

public class ApiUserDto extends ApiAuthRequestDto {

	private String role;
	private String endpoints;
	
	public ApiUserDto(String username, String password, String role, String endpoints) {
		super(username, password);
		this.role = role;
		this.endpoints = endpoints;
	}

	public String getRole() {
		return role;
	}

	public String getEndpoints() {
		return endpoints;
	}
	
}
