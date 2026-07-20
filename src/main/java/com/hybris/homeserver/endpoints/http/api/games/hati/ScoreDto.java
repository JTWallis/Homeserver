package com.hybris.homeserver.endpoints.http.api.games.hati;

public class ScoreDto {
	private String name;
	private Long score;
		
	public ScoreDto() {}
	
	public ScoreDto(String name, long score) {
		this.name = name;
		this.score = score;
	}
	
	public String getName() {
		return name;
	}
	
	public Long getScore() {
		return score;
	}
}
