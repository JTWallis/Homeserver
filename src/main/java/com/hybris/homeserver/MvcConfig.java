package com.hybris.homeserver;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("cloud");
		registry.addViewController("/login").setViewName("login");
		registry.addViewController("/cloud").setViewName("cloud");
		registry.addViewController("cloud/login").setViewName("cloud_login");
	}
}
