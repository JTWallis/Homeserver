package com.hybris.homeserver.endpoints.websocket.pictochat.connection;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class UserConnectionController {
	
	@Autowired
	private UsernameTracker userTracker;
	
	@Autowired
	private SimpMessagingTemplate template;

	/**
	 * Users can register themselves with their client-nickname, by publishing it to the endpoint /app/register.
	 * The UUID of the user will be linked to that nickname, thus allowing for identical nicknames of different users.
	 * On publishing to this endpoint, users should also subscribe to the endpoint /user/queue/receipts
	 *  and await for the confirmation message to arrive to this endpoint.
	 * This is done due to racing-conditions, as a UserConnection message may be broadcast, before the
	 *  nickname was linked, even if the register-publish arrives before the room-number subscription.
	 *  As a consequence, the clients would then receive a joining user message with the nickname NULL.
	 * @param nickname Client nickname to link with the UUID.
	 * @param principal Unique user id.
	 */
	@MessageMapping("/register")
	public void registerUser(String nickname, Principal principal) {
		System.out.println("Registering User " + nickname + " to UUID " + principal.getName());
		userTracker.link(principal.getName(), nickname);
		
		UserRegister userRegister = new UserRegister(principal.getName(), "ok");
		
		template.convertAndSendToUser(principal.getName(), "/queue/receipts", userRegister);
	}
	
}
