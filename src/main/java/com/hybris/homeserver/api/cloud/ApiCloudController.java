package com.hybris.homeserver.api.cloud;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiCloudController {

	@GetMapping("/api/cloud")
	public String getCloud() {
		return "cloud";
	}
}
