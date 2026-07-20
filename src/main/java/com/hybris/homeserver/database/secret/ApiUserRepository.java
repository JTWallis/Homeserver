package com.hybris.homeserver.database.secret;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiUserRepository extends JpaRepository<ApiUserEntity, Long> {

	@Query("SELECT e from ApiUserEntity e "
			+ "WHERE e.username = :username")
	ApiUserEntity findByUsername(String username);
}
