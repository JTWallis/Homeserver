package com.hybris.homeserver.endpoints.http.api.secret.user;

public class ApiUserResponseDto {

	private String username;
	private String role;
	private String endpoints;
	
	public ApiUserResponseDto(String username, String role, String endpoints) {
		this.username = username;
		this.role = role;
		this.endpoints = endpoints;
	}
	
	public ApiUserResponseDto(ApiUserDto apiUserDto) {
		this(apiUserDto.getUsername(), apiUserDto.getRole(), apiUserDto.getEndpoints());
	}

	public String getUsername() {
		return username;
	}

	public String getRole() {
		return role;
	}

	public String getEndpoints() {
		return endpoints;
	}
}
