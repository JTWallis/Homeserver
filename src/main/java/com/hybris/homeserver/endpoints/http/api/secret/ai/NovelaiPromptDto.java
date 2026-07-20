package com.hybris.homeserver.endpoints.http.api.secret.ai;

public class NovelaiPromptDto {

	private String prompt;
	
	public NovelaiPromptDto() {}
	
	public NovelaiPromptDto(String prompt) {
		this.prompt = prompt;
	}
	
	public String getPrompt() {
		return prompt;
	}
}
