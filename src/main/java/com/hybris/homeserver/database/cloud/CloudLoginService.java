package com.hybris.homeserver.database.cloud;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.hybris.homeserver.JwtService;
import com.hybris.homeserver.endpoints.http.api.cloud.ApiCloudAuthResponseDto;
import com.hybris.homeserver.endpoints.http.cloud.CloudLoginDto;
import com.hybris.homeserver.endpoints.http.cloud.CloudUserDetailsService;

@Service
public class CloudLoginService {
	
	private final CloudLoginRepository loginRepository;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final CloudUserDetailsService cloudUserDetailsService;
	
	public CloudLoginService(
			CloudLoginRepository loginRepository,
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			CloudUserDetailsService cloudUserDetailsService
	) {
		this.loginRepository = loginRepository;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.cloudUserDetailsService = cloudUserDetailsService;
	}
	
	public CloudLoginEntity addUser(CloudLoginDto login) throws DataIntegrityViolationException {
		login = new CloudLoginDto(
				login.getUsername(),
				BCrypt.hashpw(login.getPassword(), BCrypt.gensalt())
		);
		
		CloudLoginEntity entity = new CloudLoginEntity();
		BeanUtils.copyProperties(login, entity);
		
		try {
			loginRepository.save(entity);
		} catch(Exception e) {
			// Unique constraint violation.
			// Sadly no cleaner approach to catch this exception.
			//   StackTrace is:
			//   -> org.hibernate.exception.GenericJDBCException
			//   -> org.sqlite.SQLiteException
			for(Throwable t = e; t != null; t = t.getCause()) {
				if(t.getMessage().toUpperCase().contains("SQLITE_CONSTRAINT_UNIQUE")) {
					throw new DataIntegrityViolationException(t.getMessage());
				}
			}
		}
		
		return entity;
	}
	
	public ApiCloudAuthResponseDto authenticate(CloudLoginDto login) throws AuthenticationException {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						login.getUsername(),
						login.getPassword()
				)
		);
		
		var user = cloudUserDetailsService.loadUserByUsername(login.getUsername());
		String authToken = jwtService.generateToken(user);
		
		return new ApiCloudAuthResponseDto(authToken, login.getUsername());
	}
}
