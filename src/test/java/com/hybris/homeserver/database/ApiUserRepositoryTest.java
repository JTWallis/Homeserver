package com.hybris.homeserver.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.orm.jpa.JpaSystemException;

import com.hybris.homeserver.database.secret.ApiUserEntity;
import com.hybris.homeserver.database.secret.ApiUserRepository;


@DataJpaTest(properties = {
		"spring.datasource.url=jdbc:sqlite:test.db"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ApiUserRepositoryTest {
	
	@Autowired
	private ApiUserRepository userRepository;
	
	private ApiUserEntity userEntity;
	
	@BeforeEach
	void setUp() {
		userEntity = new ApiUserEntity();
		userEntity.setId(100L);
		userEntity.setUsername("Huginn");
		userEntity.setPassword("hunter2");
		userEntity.setRole("USER");
		userEntity.setEndpoints("cloud");
	}
	
	@Test
	void testSave() {
		ApiUserEntity newUser = new ApiUserEntity();
		newUser.setUsername("Huginn");
		newUser.setPassword("odinsBeard");
		newUser.setRole("USER");
		newUser.setEndpoints("cloud");
		
		userRepository.save(newUser);

		assertThat(userRepository.findAll()).hasSize(1);
	}
	
	@Test
	void testUsernameUniqueConstraint() {
		ApiUserEntity user1 = new ApiUserEntity();
		user1.setUsername("Huginn");
		user1.setPassword("odinsBeard");
		user1.setRole("USER");
		user1.setEndpoints("cloud");
		
		ApiUserEntity user2 = new ApiUserEntity();
		user2.setUsername("Huginn");
		user2.setPassword("hunter2");
		user2.setRole("USER");
		user2.setEndpoints("barcode");
		
		Exception exc = assertThrows(JpaSystemException.class, () -> {
			userRepository.save(user1);
			userRepository.save(user2);
		});
		
		String expectedMessage = "could not execute statement [[SQLITE_CONSTRAINT_UNIQUE] A UNIQUE constraint failed (UNIQUE constraint failed: api_user.username)]";
	
		assertTrue(exc.getMessage().contains(expectedMessage));
	}
}
