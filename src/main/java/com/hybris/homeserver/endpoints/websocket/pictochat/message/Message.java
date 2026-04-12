package com.hybris.homeserver.endpoints.websocket.pictochat.message;

public class Message {

	private DrawCommand[] commands;
	private String creatorName;
	
	public Message() {
		
	}
	
	public Message(DrawCommand[] commands, String creatorName) {
		this.commands = commands;
		this.creatorName = creatorName;
	}
	
	public DrawCommand[] getCommands() {
		return this.commands;
	}
	
	public String getCreatorName() {
		return this.creatorName;
	}
	
	@Override
	public String toString() {
		String result = "Message:\n  Creator[" + creatorName + "]";
		for(DrawCommand command : commands) {
			result += "\n  " + command.toString();
		}
		return result;
	}
	
}
