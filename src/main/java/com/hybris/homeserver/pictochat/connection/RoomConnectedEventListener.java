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

}
