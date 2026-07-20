package com.hybris.homeserver.endpoints.http.api.games.hati;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hybris.homeserver.endpoints.http.api.ErrorResponseDto;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/games/hati")
public class ScoreController {
	
	private final ScoreService scoreService;

	
	public ScoreController(ScoreService scoreService) {
		this.scoreService = scoreService;
	}

	@PostMapping
	public ResponseEntity<?> postScore(@RequestBody ScoreDto playerScore, HttpServletRequest request) {
		ResponseEntity<?> validated = validateScore(playerScore, request);
		if(validated.getStatusCode() != HttpStatus.OK) {
			return validated;
		}
		
		List<ScoreResponseDto> scoreboard = scoreService.saveScore(playerScore);
		return ResponseEntity
				.ok()
				.body(scoreboard);
	}
	
	@GetMapping
	public ResponseEntity<?> getScoreboard(@RequestParam("name") String playerName, @RequestParam("score") Long playerScore, HttpServletRequest request) {
		ScoreDto dto = new ScoreDto(playerName, playerScore);
		ResponseEntity<?> validated = validateScore(dto, request);
		if(validated.getStatusCode() != HttpStatus.OK) {
			return validated;
		}
		
		List<ScoreResponseDto> scoreboard = scoreService.readScoreboard(dto);
		return ResponseEntity
				.ok()
				.body(scoreboard);
	}
	
	@GetMapping("/topten")
	public ResponseEntity<?> getTopTen() {
		List<ScoreResponseDto> scoreboard = scoreService.readTopTen();
		return ResponseEntity
				.ok()
				.body(scoreboard);
	}
	
	
	private ResponseEntity<?> validateScore(ScoreDto score, HttpServletRequest request) {
		if(score == null) {
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, request.getRequestURI(), "Bad DTO! Need a name and score."));
		}
		
		if(score.getName() == null || score.getName().isBlank()) {
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, request.getRequestURI(), "Name cannot be empty!"));
		}
		
		if(score.getScore() == null) {
			return ResponseEntity
					.status(HttpStatus.NOT_ACCEPTABLE)
					.body(new ErrorResponseDto(HttpStatus.NOT_ACCEPTABLE, request.getRequestURI(), "Score cannot be empty!"));
		}
		
		return ResponseEntity.ok(null);
	}
}
