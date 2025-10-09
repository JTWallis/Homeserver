package com.hybris.homeserver.pictochat.connection;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class RoomSubscriptionTracker {

	private final Character[] ROOM_PREFIXES = {'a', 'b', 'c', 'd'};
	private Map<String, Character> userToRoom = new ConcurrentHashMap<>();
	
	public boolean isRoomValid(char room) {
		for(char prefix : ROOM_PREFIXES) {
			if(prefix == room) return true;
		}
		
		return false;
	}
	
	public boolean isSubscribed(String user) {
		return userToRoom.get(user) != null;
	}
	
	public boolean isSubscribedTo(String user, char roomNumber) {
		Character room = userToRoom.get(user);
		if(room == null) return false;
		
		return room.equals(roomNumber);
	}
	
	public Character getSubscribedRoom(String user) {
		return userToRoom.get(user);
	}
	
	public void subscribe(String user, String room) {
		System.out.println("User " + user + " connected to room " + room);
		userToRoom.put(user, room);
	}
	
	public void unsubscribe(String user) {
		System.out.println("User " + user + " disconnected from a room.");
		userToRoom.remove(user);
	}
	
	public Rooms createRoomsDto() {
		Room[] rooms = new Room[ROOM_PREFIXES.length];
		for(int i = 0; i < ROOM_PREFIXES.length; i++) {
			Character roomPrefix = ROOM_PREFIXES[i];
			int roomCount = Collections.frequency(userToRoom.values(), roomPrefix);
			rooms[i] = new Room(roomPrefix, roomCount);
		}
		
		return new Rooms(rooms);
	}
}
