package com.hybris.homeserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hybris.homeserver.JwtAuthenticationFilter;
import com.hybris.homeserver.endpoints.http.api.ErrorResponseDto;
import com.hybris.homeserver.endpoints.http.api.secret.auth.ApiUser;
import com.hybris.homeserver.endpoints.http.api.secret.auth.ApiUserDetailsService;

import jakarta.servlet.http.HttpServletResponse;

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
				.anyRequest().access(whitelistAuthorizationManager()))
		
		.userDetailsService(userDetailsService)
		.formLogin(form -> form
				.loginPage("/cloud/login")
				.loginProcessingUrl("/cloud/login")
				.defaultSuccessUrl("/cloud", true)
				.failureUrl("/cloud/login?error")
				.permitAll()
		)
		.logout(logout -> logout
				.logoutUrl("/cloud/logout"))
		
		.exceptionHandling(ex -> ex
				.accessDeniedHandler((request, response, accessDeniedException) ->
				response.sendRedirect("cloud/login?error=denied"))
		);
		
		return http.build();
	}
	
	@Bean
	@Order(2)
	public SecurityFilterChain secretApiFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthFilter
	) throws Exception {
		http
		.csrf(csrf -> csrf.disable())
		.securityMatcher("/api/secret/**")
		.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/secret/auth/**").permitAll()
				.anyRequest().access(whitelistAuthorizationManager()))
		
		.userDetailsService(userDetailsService)
		
		.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		
		.authenticationProvider(cloudAuthenticationProvider())
		
		.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
		
		.exceptionHandling(ex -> ex.accessDeniedHandler(apiAccessDeniedHandler()));
		
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
	
	@Bean
	public AuthorizationManager<RequestAuthorizationContext> whitelistAuthorizationManager() {
		return (authSupplier, context) -> {
			Authentication auth = authSupplier.get();
			if(auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof ApiUser principal)) {
				return new AuthorizationDecision(false);
			}

			boolean granted = principal.isAdmin();
			if(!granted) {
				String path = context.getRequest().getRequestURI();
				granted = principal.isWhitelisted(path);
			}

			return new AuthorizationDecision(granted);
		};
	}
	
	@Bean
	public AccessDeniedHandler apiAccessDeniedHandler() {
		return (request, response, accessDeniedException) -> {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			
			String message;
			if(accessDeniedException instanceof AuthorizationDeniedException) {
				message = "Insufficient user permission for this endpoint";
			} else {
				message = "Access denied";
			}
			
			ErrorResponseDto body = new ErrorResponseDto(
					HttpStatus.FORBIDDEN,
					request.getRequestURI(),
					message
			);
				
			ObjectMapper om = new ObjectMapper();
			om.writeValue(response.getWriter(), body);
		};
	}
}
