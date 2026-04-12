package com.hybris.homeserver.endpoints.websocket.pictochat.connection;

public class Rooms {

	private Room[] rooms;
	
	public Rooms() {}
	
	public Rooms(Room[] rooms) {
		this.rooms = rooms;
	}
	
	public Room[] getRooms() {
		return rooms;
	}
	
	@Override
	public String toString() {
		String result = "Rooms:\n";
		for(Room room : rooms) {
			result += "  " + room.toString();
		}
		
		return result;
	}
}
