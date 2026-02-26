package com.hybris.homeserver.pictochat.message;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

	@Autowired
	private SimpMessagingTemplate template;
	
	@Autowired
	private SimpUserRegistry userRegistry;
	
	/**
	 * Echo a message sent to /messages back to all subscribers of /room/a/messages.
	 */
	@MessageMapping("/messages")
	@SendTo("/topic/room/a/messages")
	public Message sendMessage(Message message, Principal principal) throws Exception {
		System.out.println("Received and echoing back Message from " + principal.getName() + "\n" + message.toString());
		
		for(SimpUser user : userRegistry.getUsers()) {
			String msg;
			if(user.getName().equals(principal.getName())) {
				msg = "YOU! ARE! NOT! THE ONE!";
			} else {
				msg = "YOU HAVE BEEN CHOSEN!";
			}
			
			template.convertAndSendToUser(user.getName(), "/queue/reply", msg);

		}
		
		MessageBroadcast messageBroadcast = new MessageBroadcast(message, principal.getName());
		
		return messageBroadcast;
	}
}
