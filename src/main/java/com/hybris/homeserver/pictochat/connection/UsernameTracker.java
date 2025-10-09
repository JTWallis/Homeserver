package com.hybris.homeserver.pictochat.connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class UsernameTracker {

	private Map<String, String> uuidToNickname = new ConcurrentHashMap<>();
	
	public void link(String uuid, String nickname) {
		uuidToNickname.put(uuid, nickname);
	}
	
	public void unlink(String uuid) {
		uuidToNickname.remove(uuid);
	}
	
	public String getNickname(String uuid) {
		return uuidToNickname.get(uuid);
	}
	
}
