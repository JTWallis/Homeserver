package com.hybris.homeserver.pictochat.connection;

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

	@MessageMapping("/register")
	public void registerUser(String nickname, Principal principal) {
		System.out.println("Registering User " + nickname + " to UUID " + principal.getName());
		userTracker.link(principal.getName(), nickname);
		
		template.convertAndSendToUser(principal.getName(), "/queue/receipts", "ok");
	}
	
}
