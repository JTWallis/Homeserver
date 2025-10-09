package com.hybris.homeserver.pictochat.connection;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class RoomSubscriptionTracker {

	private final String[] ROOM_PREFIXES = {"a", "b", "c", "d"};
	private Map<String, String> userToRoom = new ConcurrentHashMap<>();
	
	public void subscribe(String user, String room) {
		System.out.println("User " + user + " connected to room " + room);
		userToRoom.put(user, room);
	}
	
	public void unsubscribe(String user) {
		System.out.println("User " + user + " disconnected from a room.");
		userToRoom.remove(user);
	}

}
