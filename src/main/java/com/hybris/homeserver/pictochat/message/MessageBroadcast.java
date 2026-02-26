package com.hybris.homeserver.pictochat.message;

public class MessageBroadcast extends Message {

	private String creatorUuid;
	
	public MessageBroadcast() {}

	
	public MessageBroadcast(DrawCommand[] commands, String creatorName, String creatorUuid) {
		super(commands, creatorName);
		this.creatorUuid = creatorUuid;
	}
	
	public MessageBroadcast(Message message, String creatorUuid) {
		this(message.getCommands(), message.getCreatorName(), creatorUuid);
	}
	
	public String getCreatorUuid() {
		return creatorUuid;
	}
}
