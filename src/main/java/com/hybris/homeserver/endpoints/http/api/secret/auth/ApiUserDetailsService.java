package com.hybris.homeserver.endpoints.http.api.secret.auth;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hybris.homeserver.database.secret.ApiUserEntity;
import com.hybris.homeserver.database.secret.ApiUserRepository;

@Service
public class ApiUserDetailsService implements UserDetailsService {

	private final ApiUserRepository loginRepository;

	public ApiUserDetailsService(ApiUserRepository loginRepository) {
		this.loginRepository = loginRepository;
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		ApiUserEntity entity = findByUsername(username);
		
		UserDetails userDetails = 
				User.builder()
				.username(entity.getUsername())
				.password(entity.getPassword())
				.roles(entity.getRole())
				.build();
		
		return new ApiUser(userDetails, entity.getEndpoints());
	}
	
	public ApiUserEntity findByUsername(String username) throws UsernameNotFoundException {
		ApiUserEntity entity = loginRepository.findByUsername(username);
		
		if(entity == null) {
			throw new UsernameNotFoundException("No such user " + username);
		}
		
		return entity;
	}
	
	
}
