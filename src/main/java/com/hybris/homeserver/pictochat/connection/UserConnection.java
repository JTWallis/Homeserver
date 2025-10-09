package com.hybris.homeserver.pictochat.connection;

public class UserConnection {

	private String nickname;
	private ConnectionTypes connectionType;
	
	public UserConnection() {}
	
	public UserConnection(String nickname, ConnectionTypes connectionType) {
		this.nickname = nickname;
		this.connectionType = connectionType;
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
