package com.hybris.homeserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hybris.homeserver.JwtAuthenticationFilter;
import com.hybris.homeserver.endpoints.http.api.secret.auth.ApiUserDetailsService;

@Configuration
public class SecurityConfig {
	
	private final ApiUserDetailsService userDetailsService;
	
	public SecurityConfig(ApiUserDetailsService userDetailService) {
		this.userDetailsService = userDetailService;
	}
	
	@Bean
	@Order(1)
	public SecurityFilterChain cloudFilterChain(HttpSecurity http) throws Exception {
		
		http
		.csrf(csrf -> csrf.disable())
		.securityMatcher("/cloud/**")
		.authorizeHttpRequests(auth -> auth
				.requestMatchers("/cloud/login", "/cloud/register").permitAll()
				.anyRequest().authenticated())
		
		.userDetailsService(userDetailsService)
		.formLogin(form -> form
				.loginPage("/cloud/login")
				.loginProcessingUrl("/cloud/login")
				.defaultSuccessUrl("/cloud", true)
				.failureUrl("/cloud/login?error")
				.permitAll()
		)
		.logout(logout -> logout
				.logoutUrl("/cloud/logout"));
		return http.build();
	}
	
	@Bean
	@Order(2)
	public SecurityFilterChain cloudApiFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthFilter
	) throws Exception {
		http
		.csrf(csrf -> csrf.disable())
		.securityMatcher("/api/cloud/**")
		.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/cloud/auth/**").permitAll()
				.anyRequest().authenticated())
		
		.userDetailsService(userDetailsService)
		
		.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		
		.authenticationProvider(cloudAuthenticationProvider())
		
		.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}

	@Bean
	@Order(3)
	public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
		http
		.csrf(csrf -> csrf.disable())
		.authorizeHttpRequests(authz -> authz
					.requestMatchers("/**").permitAll()
					.anyRequest().denyAll()
		)
		/*
		.formLogin(form -> form
				.loginPage("/api/login")
				.permitAll()
		);*/
			;
		return http.build();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public AuthenticationProvider cloudAuthenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	

}
