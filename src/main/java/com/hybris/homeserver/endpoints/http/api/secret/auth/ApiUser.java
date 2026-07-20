package com.hybris.homeserver.endpoints.http.api.secret.auth;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class ApiUser extends User {
	private static final long serialVersionUID = 4022516152719060950L;
	
	private final String endpoints;
	
	public ApiUser(UserDetails user, String endpoints) {
		super(user.getUsername(), user.getPassword(), user.getAuthorities());
		this.endpoints = endpoints;
	}
	
	public boolean isAdmin() {
		for(var auth : getAuthorities()) {
			String authString = auth.getAuthority();
			if(authString != null && authString.equals("ROLE_ADMIN")) {
				return true;
			}
		}
		
		return false;
	}

	public boolean isWhitelisted(String path) {
		if(path == null || path.isBlank()) return false;
		
		String apiSecret = "/api/secret";
		if(path.startsWith(apiSecret)) {
			path = path.substring(apiSecret.length());
		}
		
		String[] split = path.split("/");
		if(split.length < 1) return false;
		
		String endpoint = split[1];
		for(String s : getWhitelist()) {
			if(s.equals(endpoint)) return true;
		}
		
		return false;
	}
	
	public String[] getWhitelist() {
		return endpoints.split(";");
	}

}
