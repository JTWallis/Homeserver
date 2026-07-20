package com.hybris.homeserver.endpoints.http.api.secret.user;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.hybris.homeserver.database.secret.ApiUserEntity;
import com.hybris.homeserver.database.secret.ApiUserRepository;

@Service
public class ApiUserService {

	private final ApiUserRepository userRepository;
	
	public ApiUserService(ApiUserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	public void registerUser(ApiUserDto userDto) throws DataIntegrityViolationException {
		
		ApiUserDto persistedDto = new ApiUserDto(
				userDto.getUsername(),
				BCrypt.hashpw(userDto.getPassword(), BCrypt.gensalt()),
				userDto.getRole(),
				userDto.getEndpoints()
		);
		
		ApiUserEntity entity = new ApiUserEntity();
		BeanUtils.copyProperties(persistedDto, entity);

		try {
			userRepository.save(entity);
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
	}
}
