package com.hybris.homeserver.pictochat.connection;

public class Room {

	private String roomNumber;
	private int connectionCount;
	
	public Room() {}
	
	public Room(String roomNumber, int connectionCount) {
		this.roomNumber = roomNumber;
		this.connectionCount = connectionCount;
	}
	
	public String getRoomNumber() {
		return roomNumber;
	}
	
	public int getConnectionCount() {
		return connectionCount;
	}
	
	@Override
	public String toString() {
		return String.format("Room[%10s] Count[%2d]", roomNumber, connectionCount);
	}
}
