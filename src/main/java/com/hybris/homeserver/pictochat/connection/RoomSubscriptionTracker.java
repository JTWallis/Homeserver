package com.hybris.homeserver.pictochat.connection;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class RoomSubscriptionTracker {

	private final String[] ROOM_PREFIXES = {"a", "b", "c", "d"};
	private Map<String, String> userToRoom = new ConcurrentHashMap<>();
	
	public boolean isRoomValid(String room) {
		for(String prefix : ROOM_PREFIXES) {
			if(prefix.equals(room)) return true;
		}
		
		return false;
	}
	
	public boolean isSubscribed(String user) {
		return userToRoom.get(user) != null;
	}
	
	public boolean isSubscribedTo(String user, String roomNumber) {
		String room = userToRoom.get(user);
		if(room == null) return false;
		
		return room.equals(roomNumber);
	}
	
	public String getSubscribedRoom(String user) {
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

}
