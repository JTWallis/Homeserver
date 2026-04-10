package com.hybris.homeserver.database.cloud;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.hybris.homeserver.cloud.CloudLoginDto;

@Service
public class CloudLoginService {
	
	private final CloudLoginRepository loginRepository;
	
	public CloudLoginService(CloudLoginRepository loginRepository) {
		this.loginRepository = loginRepository;
	}
	
}
