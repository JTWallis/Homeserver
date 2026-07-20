package com.hybris.homeserver.endpoints.http.api.secret.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.hybris.homeserver.JwtService;
import com.hybris.homeserver.database.secret.ApiUserRepository;
import com.hybris.homeserver.endpoints.http.api.AuthResponseDto;

@Service
public class ApiAuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final ApiUserDetailsService userDetailsService;
	
	public ApiAuthService(
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			ApiUserDetailsService userDetailsService
	) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}
	
	public AuthResponseDto authenticate(ApiAuthRequestDto authRequest) throws AuthenticationException {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						authRequest.getUsername(),
						authRequest.getPassword()
				)
		);
		
		UserDetails user = userDetailsService.loadUserByUsername(authRequest.getUsername());
		String authToken = jwtService.generateToken(user);
		
		return new AuthResponseDto(authToken);
	}

}
