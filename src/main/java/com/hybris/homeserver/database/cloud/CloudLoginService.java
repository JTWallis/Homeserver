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
	

	public boolean isLoginValid(String username, String password) throws EmptyResultDataAccessException {
		String storedPassword = loginRepository.getPasswordByUsername(username);
		
		if(storedPassword == null || storedPassword.isEmpty()) {
			throw new EmptyResultDataAccessException(1);
		}
		
		return BCrypt.checkpw(password, storedPassword);
	}
}
