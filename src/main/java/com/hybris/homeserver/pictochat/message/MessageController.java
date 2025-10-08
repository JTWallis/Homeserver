package com.hybris.homeserver.pictochat.message;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

	/**
	 * Echo a message sent to /messages back to all subscribers of /room/a/messages.
	 */
	@MessageMapping("/messages")
	@SendTo("/topic/room/a/messages")
	public Message sendMessage(Message message, Principal principal) throws Exception {
		System.out.println("Received and echoing back Message from " + principal.getName() + "\n" + message.toString());
		return message;
	}
}
