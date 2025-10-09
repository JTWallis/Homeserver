package com.hybris.homeserver.pictochat.connection;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class RoomSubscriptionTracker {

	private final String[] ROOM_PREFIXES = {"a", "b", "c", "d"};
	private Map<String, String> userToRoom = new ConcurrentHashMap<>();

}
