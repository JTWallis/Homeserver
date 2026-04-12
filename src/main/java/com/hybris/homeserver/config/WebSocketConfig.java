package com.hybris.homeserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import com.hybris.homeserver.StompHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	private final int MESSAGE_SIZE_LIMIT = 1024 * 512;
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic", "/queue");
		config.setApplicationDestinationPrefixes("/app");
	}
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/pictochat")
				.setAllowedOrigins("http://localhost:8001/")
				.setHandshakeHandler(new StompHandshakeHandler());
	}
	
	
	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
		registry.setMessageSizeLimit(MESSAGE_SIZE_LIMIT);
		registry.setSendBufferSizeLimit(MESSAGE_SIZE_LIMIT);
		registry.setSendTimeLimit(20 * 1000);
	}
	
	
	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(MESSAGE_SIZE_LIMIT);
		container.setMaxBinaryMessageBufferSize(MESSAGE_SIZE_LIMIT);
		return container;
	}
	
}
