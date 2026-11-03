package com.tekmaturix.jiramcptoolkitclient.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tekmaturix.jiramcptoolkitclient.service.JiraMcpToolkitClientService;

@RestController
@RequestMapping("/mcp/jira/api/v1")
public class JiraMcpToolkitClientController {
	
	private final JiraMcpToolkitClientService jiraMcpToolkitClientService;
	
	public JiraMcpToolkitClientController(JiraMcpToolkitClientService jiraMcpToolkitClientService) {
		super();
		this.jiraMcpToolkitClientService = jiraMcpToolkitClientService;
	}
	
	@GetMapping("/call")
	public String call(@RequestParam("prompt") String promptQuery) {
		return this.jiraMcpToolkitClientService.call(promptQuery);
	}

}
