package com.hybris.homeserver.endpoints.http.api.games.hati;

public class ScoreResponseDto extends ScoreDto {

	private Long rank;
	
	public ScoreResponseDto() {}
	
	public ScoreResponseDto(String name, long score, long rank) {
		super(name, score);
		this.rank = rank;
	}
	
	public Long getRank() {
		return rank;
	}
}
