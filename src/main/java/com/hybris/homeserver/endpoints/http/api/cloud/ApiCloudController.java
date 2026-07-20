package com.hybris.homeserver.endpoints.http.api.cloud;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiCloudController {

	@GetMapping("/api/secret/cloud")
	public String getCloud() {
		return "cloud";
	}
}
