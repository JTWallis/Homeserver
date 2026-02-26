package com.hybris.homeserver.pictochat.connection;

public class UserConnection {

	private String uuid;
	private String nickname;
	private ConnectionTypes connectionType;
	
	public UserConnection() {}
	
	public UserConnection(String uuid, String nickname, ConnectionTypes connectionType) {
		this.uuid = uuid;
		this.nickname = nickname;
		this.connectionType = connectionType;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public ConnectionTypes getConnectionType() {
		return connectionType;
	}
	
	@Override
	public String toString() {
		return String.format("User[%12s] ConnectionType[%10s]", nickname, connectionType);
	}
}
