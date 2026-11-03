package com.tekmaturix.jiramcptoolkitserver.configuration;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.context.annotation.Configuration;

import com.tekmaturix.jiramcptoolkitserver.constants.JiraManagementConstants;

@Configuration
public class JiraManagementHttpClientConfiguration {

	private final HttpClient httpClient;

	private final String encodedAuth;

	private final String baseUrl;

	public JiraManagementHttpClientConfiguration() {
		super();
		this.httpClient = HttpClient.newHttpClient();
		String auth = JiraManagementConstants.USER_NAME + ":" + JiraManagementConstants.API_TOKEN;
		this.encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
		this.baseUrl = JiraManagementConstants.BASE_URL;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public String getAuthorizationHeader() {
		return "Basic " + encodedAuth;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

}
