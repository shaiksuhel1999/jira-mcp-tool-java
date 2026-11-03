package com.tekmaturix.jiramcptoolkitserver.configuration;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.tekmaturix.jiramcptoolkitserver.constants.JiraManagementConstants;

@Configuration
public class JiraManagementRestClient {

	@Bean
	public JiraRestClient restClient() {
		return new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(
				URI.create(JiraManagementConstants.BASE_URL), JiraManagementConstants.USER_NAME,
				JiraManagementConstants.API_TOKEN);
	}

}
