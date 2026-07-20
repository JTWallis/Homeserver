package com.hybris.homeserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@WebAppConfiguration	// Necessary to not throw exception in WebSocketConfig: https://stackoverflow.com/a/73614454
class HomeserverApplicationTests {

	@Test
	void contextLoads() {
	}

}
