package com.deepthink.org.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class DeepthinkSimpleServiceImpl implements DeepthinkSimpleService{
//	@Autowired
	private ChatClient client;

	public DeepthinkSimpleServiceImpl(ChatClient.Builder builder) {
		this.client = builder.build();
	}

	@Override
	public Flux<String> getResponseByStream(String prompt) {
		String formattedPrompt = autoFormatPrompt(prompt);
		return client.prompt().user(formattedPrompt).stream().content();
	}

	private String autoFormatPrompt(String prompt) {
		if (prompt.toLowerCase().contains("resume")) {
			return "Provide a well-structured resume format for: " + prompt;
		} else if (prompt.toLowerCase().contains("algorithm") || prompt.toLowerCase().contains("code")) {
			return "Provide the following as a properly formatted code snippet: " + prompt;
		} else if (prompt.toLowerCase().contains("explain") || prompt.toLowerCase().contains("describe")) {
			return "Format the response in well-structured paragraphs: " + prompt;
		} else {
			return "Format the response in a simple text format: " + prompt;
		}
	}
}
