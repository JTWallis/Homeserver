package com.hybris.homeserver;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hybris.homeserver.cloud.CloudLoginController;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(CloudLoginController.class);
	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;
	
	public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}
	
	@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		try {
			String authHeader = request.getHeader("Authorization");
			
			// Skip filter if no Bearer Token.
			if(authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.substring(7).isBlank()) {
				filterChain.doFilter(request, response);
				return;
			}
			
			final String jwt = authHeader.substring(7);
			final String username = jwtService.extractUsername(jwt);
			
			// Username exists but no authentication yet
			if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
				
				if(jwtService.isTokenValid(jwt, userDetails)) {
					System.out.println("Auth Valid Token");
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
							userDetails,
							null,
							userDetails.getAuthorities()
					);
					
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
			
			filterChain.doFilter(request, response);
		} catch(ExpiredJwtException e) {
			setErrorResponse(response, request.getRequestURI(), HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
		} catch(MalformedJwtException | IllegalArgumentException e) {
			setErrorResponse(response, request.getRequestURI(), HttpServletResponse.SC_BAD_REQUEST, "Invalid token");
		} catch(Exception e) {
			logger.warn("Authentication error: " + e.getMessage());
			setErrorResponse(response, request.getRequestURI(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error");
		}
	}
	
	private void setErrorResponse(HttpServletResponse response, String path, int sc, String message) throws IOException {
		response.setStatus(sc);
		response.setContentType("application/json");
		
		HttpStatus status = HttpStatus.valueOf(sc);
		ErrorResponseDto errorDto = new ErrorResponseDto(status, path, message);
		
		new ObjectMapper().writeValue(response.getOutputStream(), errorDto);
	}

}
