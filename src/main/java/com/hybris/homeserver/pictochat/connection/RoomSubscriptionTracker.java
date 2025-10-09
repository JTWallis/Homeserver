package com.hybris.homeserver.pictochat.connection;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Manages subscriptions of a user's UUID to a single, valid chat room number.
 * A user can only subscribe to one room at a time.
 * To disconnect/unsubscribe from a chatroom, the frontend would have to manually unsubscribe the endpoint
 *   or terminate the WebSocket connection.
 */
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
	
	/**
	 * Links a user to the given room number.
	 * The room number should be checked isRoomValid beforehand.
	 * If the user is already linked to another room, the passed room will override the subscription.
	 * @param user UUID of the user Principal.
	 * @param room Valid room number to link the user to.
	 */
	public void subscribe(String user, Character room) {
		System.out.println("User " + user + " connected to room " + room);
		userToRoom.put(user, room);
	}
	
	/**
	 * Unlinks a user from the room they may be linked to.
	 * @param user UUID of the user Principal.
	 */
	public void unsubscribe(String user) {
		System.out.println("User " + user + " disconnected from a room.");
		userToRoom.remove(user);
	}
	
	/**
	 * Counts all users subscribed to each room and creates a Rooms DTO with that data.
	 * @return New Rooms instance with the rooms array holding a new, populated Room instance for each room number.
	 */
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
