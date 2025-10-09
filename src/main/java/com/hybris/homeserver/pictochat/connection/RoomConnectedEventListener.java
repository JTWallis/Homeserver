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
		
		roomTracker.subscribe(user, roomNumber);
		broadcastRoomsConnectionCount();
	}
	
	@EventListener
	public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		Principal principal = accessor.getUser();
		String destination = accessor.getDestination();
		
		if(principal == null || destination == null) return;
		
		String user = principal.getName();
		
		if(destination.startsWith(ENDPOINT_ROOM) && roomTracker.isSubscribed(user)) {
			roomTracker.unsubscribe(user);
			broadcastRoomsConnectionCount();
		}
	}
	
	@EventListener
	public void handleDisconnectEvent(SessionDisconnectEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		Principal principal = accessor.getUser();
		
		if(principal == null) return;
		
		String user = principal.getName();
		
		if(roomTracker.isSubscribed(user)) {
			roomTracker.unsubscribe(user);
			broadcastRoomsConnectionCount();
		}
	}
	
	private void broadcastRoomsConnectionCount() {
		template.convertAndSend(ENDPOINT_CONNECTIONS, roomTracker.createRoomsDto());
	}

}
