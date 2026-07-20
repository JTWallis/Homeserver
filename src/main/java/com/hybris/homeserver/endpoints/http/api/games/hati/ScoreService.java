package com.hybris.homeserver.endpoints.http.api.games.hati;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.hybris.homeserver.database.OffsetBasedPageRequest;
import com.hybris.homeserver.database.games.hati.ScoreEntity;
import com.hybris.homeserver.database.games.hati.ScoreRepository;

@Service
public class ScoreService {
	
	private final int TOTAL_ENTRIES = 10;
	private final int TOP_COUNT = 5;
	
	// How many entries remain for player-centered indow.
	private final int WINDOW_SIZE = TOTAL_ENTRIES - TOP_COUNT;
	
	// How many maximum neighbors to include on each side (assumes odd window size for even sides)
	private final int NEIGHBOR_OFFSET = (WINDOW_SIZE - 1) / 2;
	
	private final ScoreRepository repository;
	
	public ScoreService(ScoreRepository repository) {
		this.repository = repository;
	}
	
	/**
	 * Saves the player score in the db and returns a scoreboard based on that score.
	 * See {@link #readScoreboard(ScoreEntity)} for more details about the scoreboard structure.
	 * 
	 * The player score will be sanitized before saving it,
	 * by making sure the name is only 4 uppercase chars
	 * and the score is clamped between 0 and 999_999.
	 * @param playerScore Player score to save and lookup in the db
	 * @return Up to ten scores from the scoreboard with a top-three and the rest based on the player score
	 * @see {@link #readScoreboard(ScoreEntity)}
	 */
	public List<ScoreResponseDto> saveScore(ScoreDto playerScore) {
		String name = playerScore.getName();
		long score = playerScore.getScore();
		
		long maxScore = 999_999;

		if(name.length() > 4) name = name.substring(0, 4);
		name = name.toUpperCase();
		if(score > maxScore) score = maxScore;
		if(score < 0) score = 0;
		
		ScoreDto sanitizedDto = new ScoreDto(name, score);
		ScoreEntity entity = new ScoreEntity();
		BeanUtils.copyProperties(sanitizedDto, entity);
		ScoreEntity savedEntity = repository.save(entity);
		
		return readScoreboard(savedEntity);
	}
	
	/**
	 * Creates a list of up to ten scores.
	 * The first three entries will be the top-three highest scores.
	 * The rest of the list will be populated based on the given player score,
	 * with usually three neighboring scores above and below.
	 * 
	 * Redundant scores are avoided (e.g. if player score is within top-three).
	 * If there are not enough scores to fetch for one side,
	 * the opposite side will be populated with enough entries to fit the ten total entries.
	 * The entry count can be less than ten, if not enough scores exist.
	 * 
	 * @param playerEntity Player score to lookup in the db. Needs a valid id
	 * @return Up to ten scores from the scoreboard. Usually structured with the top-three scores,
	 * followed by three scores above the player score, the player score itself,
	 * followed by three scores below the player score
	 */
	public List<ScoreResponseDto> readScoreboard(ScoreEntity playerEntity) {
		long total = repository.count();
		int topN = (int) Math.min(TOP_COUNT, total);
		
		List<ScoreResponseDto> result = new ArrayList<>();
		
		if(topN > 0) {
			List<ScoreEntity> topEntries = repository.findAllByOrderByScoreDescIdAsc(
					PageRequest.of(0, topN));
			
			for(int i = 0; i < topEntries.size(); i++) {
				ScoreEntity entity = topEntries.get(i);
				result.add(new ScoreResponseDto(entity.getName(), entity.getScore(), i + 1));
			}
		}
		
		long playerRank = repository.countRowsAbove(playerEntity.getScore(), playerEntity.getId()) + 1;
		int windowSize = (int) Math.min(WINDOW_SIZE, Math.max(total - topN, 0));
		
		if(windowSize > 0) {
			long windowStart = Math.max(
					topN + 1,
					Math.min(playerRank - NEIGHBOR_OFFSET, total - windowSize + 1)
			);
			Pageable windowPage = new OffsetBasedPageRequest(windowStart - 1, windowSize, Sort.unsorted());
			List<ScoreEntity> windowEntries = repository.findAllByOrderByScoreDescIdAsc(windowPage);
			
			for(int i = 0; i < windowEntries.size(); i++) {
				ScoreEntity entity = windowEntries.get(i);
				result.add(new ScoreResponseDto(entity.getName(), entity.getScore(), windowStart + i));
			}
			
		}
		
		return result;
	}
	
	/**
	 * @see #readScoreboard(ScoreEntity)
	 */
	public List<ScoreResponseDto> readScoreboard(ScoreDto playerScore) {
		List<ScoreEntity> matchingEntities = repository.findScore(playerScore.getName(), playerScore.getScore(), PageRequest.of(0, 1));
		if(matchingEntities.isEmpty()) {
			// No player score that matches the name and score value. Just return top-ten.
			return readTopTen();
		}
		
		ScoreEntity playerEntity = matchingEntities.getFirst();
		return readScoreboard(playerEntity);
	}
	
	// TODO: See if JPA does caching already. Otherwise cache top ten.
	public List<ScoreResponseDto> readTopTen() {
		long total = repository.count();
		int topN = (int) Math.min(10, total);
		
		List<ScoreEntity> entities = new ArrayList<>();
		
		if(topN > 0) {
			entities.addAll(repository.findAllByOrderByScoreDescIdAsc(
					PageRequest.of(0, topN)));
		}
		
		List<ScoreResponseDto> result = new ArrayList<>();
		for(int i = 0; i < entities.size(); i++) {
			ScoreEntity entity = entities.get(i);
			result.add(new ScoreResponseDto(entity.getName(), entity.getScore(), i + 1));
		}
		return result;
	}
}
