package com.hybris.homeserver.database.cloud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CloudLoginRepository extends JpaRepository<CloudLoginEntity, Long> {

	
	@Query("SELECT c from CloudLoginEntity c "
			+ "WHERE c.username = :username")
	CloudLoginEntity getByUsername(String username);
	
	@Query("SELECT c.password FROM CloudLoginEntity c "
			+ "WHERE c.username = :username")
	String getPasswordByUsername(String username);
}
