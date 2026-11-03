package com.tekmaturix.jiramcptoolkitclient.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class JiraMcpToolkitClientService {

	private final ChatClient chatClient;

	public JiraMcpToolkitClientService(ChatClient.Builder builder, ToolCallbackProvider toolCallbackProvider) {
		super();
		this.chatClient = builder
				.defaultSystem("Please prioritise the context information before performing operations")
				.defaultToolCallbacks(toolCallbackProvider)
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build())
				.build();
	}
	
	public String call(String promptQuery) {
		PromptTemplate template = new PromptTemplate(promptQuery);
		Prompt prompt = template.create();
		return chatClient.prompt(prompt).call().content();
	}

}
