package com.hybris.homeserver.database.games.hati;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreRepository extends JpaRepository<ScoreEntity, Long> {
	
	List<ScoreEntity> findAllByOrderByScoreDescIdAsc(Pageable pageable);
	
	@Query("SELECT COUNT(s) FROM ScoreEntity s "
			+ "WHERE s.score > :score OR (s.score = :score AND s.id < :id)")
	long countRowsAbove(long score, long id);
	
	@Query("SELECT s FROM ScoreEntity s "
			+ "WHERE s.name LIKE :name AND s.score = :score")
	public List<ScoreEntity> findScore(String name, long score, Pageable pageable);
}
