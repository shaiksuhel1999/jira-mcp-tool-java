package com.tekmaturix.jiramcptoolkitserver.configuration;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tekmaturix.jiramcptoolkitserver.service.JiraManagementServiceImpl;

@Configuration
public class JiraMcpToolConfiguration {
	@Bean
	public ToolCallbackProvider toolCallbackProvider(JiraManagementServiceImpl jiraManagementServiceImpl) {
		return MethodToolCallbackProvider.builder().toolObjects(jiraManagementServiceImpl).build();
	}
}
