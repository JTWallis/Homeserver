package com.hybris.homeserver.pictochat.connection;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

/**
 * Listens to any subscriptions/unsubscriptions to the endpoint /topic/room/{number}/messages
 *  as well as disconnects from the WebSocket.
 * A valid subscription will link a user UUID to one room number at a time,
 *  broadcast the connection count of all rooms to the subscribers of /topic/connections,
 *  and broadcast the nickname of the joining user to all subscribers of that specific room,
 *  at the endpoint /topic/room/{number}/connections, excluding the joining user themselves.
 * A valid unsubscription or a disconnect will unlink the user UUID,
 *  broadcast the connection count to /topic/connections,
 *  and broadcast the nickname of the leaving user to all subs, excluding the leaving user.
 * The nickname should be established beforehand, by having the frontend publish it to the
 *  endpoint /app/register.
 */
@Component
public class RoomConnectedEventListener {
	
	private final String ENDPOINT_CONNECTIONS = "/topic/connections";
	private final String ENDPOINT_ROOM = "/topic/room/";
	private final String ENDPOINT_SUFFIX_MESSAGES = "/messages";
	private final String ENDPOINT_SUFFIX_CONNECTIONS = "/connections";
	private final String ENDPOINT_ERROR = "/queue/errors";

	@Autowired
	private RoomSubscriptionTracker roomTracker;
	
	@Autowired
	private UsernameTracker usernameTracker;
	
	@Autowired
	private SimpMessagingTemplate template;
	
	@Autowired
	private SimpUserRegistry userRegistry;
	
	/**
	 * Listens to all endpoint subscriptions and filters out the destination to the endpoint
	 *  /topic/room/{roomNumber}/messages.
	 * The user will be linked to that room number and broadcast messages will be sent to
	 *  appropriate room subscribers.
	 * @param event Event raised on a subscription request.
	 */
	@EventListener
	public void handleSubscribeEvent(SessionSubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		Principal principal = accessor.getUser();
		String destination = accessor.getDestination();
		
		System.out.println("  Subscribed to destination" + destination);
	
		if(principal == null || destination == null) return;
		if(!destination.startsWith(ENDPOINT_ROOM)) return;
		if(destination.length() == ENDPOINT_ROOM.length()) return;
		
		String user = principal.getName();
		
		char roomNumber = destination.charAt(ENDPOINT_ROOM.length());
		String endpointMessages = roomNumber + ENDPOINT_SUFFIX_MESSAGES;
		if(!destination.endsWith(endpointMessages)) return;
		
		// Subscribed to endpoint /topic/room/{number}/messages
		
		if(!roomTracker.isRoomValid(roomNumber)) {
			String errorMessage = "Tried to subscribe to invalid room number " + roomNumber + "!";
			System.out.println("ERROR: " + errorMessage);
			template.convertAndSendToUser(user, ENDPOINT_ERROR, errorMessage);
			return;
		}
		
		if(roomTracker.isSubscribed(user)) {
			System.out.println("ERROR: User " + principal.getName() + " already subscribed to a room!");
			String errorMessage = "Can only subscribe to one room at a time!";
			template.convertAndSendToUser(user, ENDPOINT_ERROR, errorMessage);
			return;
		}
		
		handleUserConnect(user, roomNumber);
		roomTracker.subscribe(user, roomNumber);
		broadcastRoomsConnectionCount();
	}
	
	/**
	 * Listens to all endpoint unsubscriptions and filters out the destination to the endpoint
	 *  /topic/room/{roomNumber}/messages.
	 * The user will be unlinked from that room number and broadcast messages will be sent to
	 *  appropriate room subscribers.
	 * @param event Event raised on an unsubscription request.
	 */
	@EventListener
	public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		Principal principal = accessor.getUser();
		String destination = accessor.getDestination();
		
		if(principal == null || destination == null) return;
		
		String user = principal.getName();
		
		if(destination.startsWith(ENDPOINT_ROOM) && roomTracker.isSubscribed(user)) {
			handleUserDisconnect(user, roomTracker.getSubscribedRoom(user));
			roomTracker.unsubscribe(user);
			broadcastRoomsConnectionCount();
		}
	}
	
	/**
	 * Listens to all disconnects from the WebSocket.
	 * The user will be unlinked from their current room number and broadcast messages will be sent to
	 *  appropriate room subscribers.
	 * @param event Event raised on a closed connection.
	 */
	@EventListener
	public void handleDisconnectEvent(SessionDisconnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		Principal principal = accessor.getUser();
		
		if(principal == null) return;
		
		String user = principal.getName();
		
		if(roomTracker.isSubscribed(user)) {
			handleUserDisconnect(user, roomTracker.getSubscribedRoom(user));
			roomTracker.unsubscribe(user);
			broadcastRoomsConnectionCount();
		}
	}
	
	/**
	 * Broadcasts a CONNECT message for a users nickname to all subscribers of the endpoint
	 *  /topic/room/{roomNumber}/connections, excluding the connected user themselves.
	 * @param user UUID of the user Principal.
	 * @param roomNumber Valid room number the user connected to.
	 */
	private void handleUserConnect(String user, char roomNumber) {
		broadcastConnect(user, roomNumber, ConnectionTypes.CONNECT);
	}
	
	/**
	 * Broadcasts a DISCONNECT message for a users nickname to all subscribers of the endpoint
	 *  /topic/room/{roomNumber}/connections, excluding the disconnected user themselves.
	 * @param user UUID of the user Principal.
	 * @param roomNumber Valid room number the user disconnected from.
	 */
	private void handleUserDisconnect(String user, char roomNumber) {
		broadcastConnect(user, roomNumber, ConnectionTypes.DISCONNECT);
	}
	
	/**
	 * Broadcasts a Rooms DTO to all subscribers of /topic/connections.
	 * The DTO will consists of an array of Room DTOs, whereas each DTO holds a
	 *  room number and count of all users currently subscribed to the endpoint
	 *  /topic/room/{roomNumber}/messages.
	 */
	private void broadcastRoomsConnectionCount() {
		template.convertAndSend(ENDPOINT_CONNECTIONS, roomTracker.createRoomsDto());
	}
	
	/**
	 * Broadcasts the CONNECT/DISCONNECT for a users nickname to all subscribers of that
	 *  connected room number at endpoint /topic/room/{roomNumber}/connections,
	 *  excluding the connecting/disconnecting user themselves.
	 * @param user UUID of the user Principal.
	 * @param roomNumber Valid room number the user connected to / disconnected from.
	 * @param connectionType Value of CONNECT or DISCONNECT.
	 */
	private void broadcastConnect(String user, char roomNumber, ConnectionTypes connectionType) {
		String endpointConnections = ENDPOINT_ROOM + roomNumber + ENDPOINT_SUFFIX_CONNECTIONS;
		String nickname = usernameTracker.getNickname(user);
		System.out.println("  Broadcasting nickname " + nickname);
		UserConnection userConnection = new UserConnection(nickname, connectionType);
		
		for(SimpUser simpUser : userRegistry.getUsers()) {
			if(simpUser.getName().equals(user)) continue;
			
			if(roomTracker.isSubscribedTo(simpUser.getName(), roomNumber)) {
				template.convertAndSend(endpointConnections, userConnection);
			}
		}
	}
	
}
