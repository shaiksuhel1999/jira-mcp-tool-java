package com.tekmaturix.jiramcptoolkitserver.model;

public record CreateRequest(String projectKey, Long issueTypeId, String issueSummary, String issueDescription,
		String assigneeName, int noOfDays) {

}
