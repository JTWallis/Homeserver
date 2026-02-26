package com.hybris.homeserver.pictochat.connection;

public class UserRegister {
	private String uuid;
	private String status;
	
	public UserRegister() {}
	
	public UserRegister(String uuid, String status) {
		this.uuid = uuid;
		this.status = status;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public String getStatus() {
		return status;
	}
}
