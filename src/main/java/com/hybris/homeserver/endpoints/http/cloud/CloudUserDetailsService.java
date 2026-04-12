package com.hybris.homeserver.endpoints.http.cloud;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hybris.homeserver.database.cloud.CloudLoginEntity;
import com.hybris.homeserver.database.cloud.CloudLoginRepository;

@Service
public class CloudUserDetailsService implements UserDetailsService {
	
	private final CloudLoginRepository loginRepository;
	
	public CloudUserDetailsService(CloudLoginRepository loginRepository) {
		this.loginRepository = loginRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		CloudLoginEntity entity;
		
		try {
			entity = findByUsername(username);
		} catch(EmptyResultDataAccessException e) {
			throw new UsernameNotFoundException("No such user '" + username + "'");
		}
		
		return User.builder()
				.username(entity.getUsername())
				.password(entity.getPassword())
				.roles("USER")
				.build();
	}
	
	private CloudLoginEntity findByUsername(String username) throws EmptyResultDataAccessException {
		CloudLoginEntity entity = loginRepository.getByUsername(username);
		
		if(entity == null) {
			throw new EmptyResultDataAccessException(1);
		}
		
		return entity;
	}

}
