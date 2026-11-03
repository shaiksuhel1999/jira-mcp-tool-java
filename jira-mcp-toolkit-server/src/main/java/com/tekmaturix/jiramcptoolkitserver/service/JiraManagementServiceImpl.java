package com.tekmaturix.jiramcptoolkitserver.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.BasicVotes;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.tekmaturix.jiramcptoolkitserver.configuration.JiraManagementHttpClientConfiguration;

// ✅ MCP Annotations

@Service
public class JiraManagementServiceImpl {

    private final JiraManagementHttpClientConfiguration clientConfiguration;
    private final JiraRestClient restClient;

    public JiraManagementServiceImpl(
            JiraManagementHttpClientConfiguration clientConfiguration,
            JiraRestClient restClient
    ) {
        this.clientConfiguration = clientConfiguration;
        this.restClient = restClient;
    }

    // ===============================
    // 🔧 MCP-Exposed Tools
    // ===============================

    @Tool(name = "createIssue", description = "Create a new Jira issue in a project")
    public String createIssue(
            @ToolParam(description = "Jira project key (e.g. 'PROJ')", required=true) String projectKey,
            @ToolParam(description = "Jira issue type ID Should be Numeric value (e.g. 10007 for 'Task', 10008 for 'Story', 10006 for 'Subtask', 10005 for 'Epic')", required=true) Long issueTypeId,
            @ToolParam(description = "Short summary or title of the issue", required=true) String issueSummary,
            @ToolParam(description = "Detailed issue description", required=true) String issueDescription,
            @ToolParam(description = "Display name of the assignee", required=true) String assigneeName,
            @ToolParam(description = "Due date offset in days", required=true) int noOfDays
    ) throws Exception {

        IssueRestClient issueRestClient = restClient.getIssueClient();
        IssueInputBuilder builder = new IssueInputBuilder(projectKey, issueTypeId);
        builder.setSummary(issueSummary);
        builder.setDescription(issueDescription);
        builder.setDueDate(new DateTime().plusDays(noOfDays));

        BasicUser assigneeUser = createUser(assigneeName);
        
        log.info("Project Key: {}",projectKey);
        log.info("IssueId: {}", issueTypeId);
        log.info("IssueSummary: {}", issueSummary);
        log.info("IssueDescription: {}", issueDescription);
        log.info("No of Days: {}", noOfDays);
        
        if (assigneeUser != null) {
            String accountId = assigneeUser.getName();
            builder.setFieldInput(new FieldInput("assignee", ComplexIssueInputFieldValue.with("id", accountId)));
            System.out.println("✅ Assignee set using accountId: " + accountId);
        }

        IssueInput input = builder.build();
        String issueKey = issueRestClient.createIssue(input).claim().getKey();
        System.out.println("✅ Issue created successfully: " + issueKey);
        return issueKey;
    }

    @Tool(name = "getIssue", description = "Get details of an existing Jira issue")
    public Issue getIssueDetails(
            @ToolParam(description = "Jira issue key (e.g. PROJ-123)", required=true) String issueKey
    ) throws Exception {
        Issue issue = restClient.getIssueClient().getIssue(issueKey).claim();
        return issue != null ? issue : null;
    }

    @Tool(name = "updateIssueDescription", description = "Update the description of an existing Jira issue")
    public void updateIssueDescription(
            @ToolParam(description = "Jira issue key to update", required=true) String issueKey,
            @ToolParam(description = "New description for the issue", required=true) String issueDescription
    ) throws Exception {
        IssueInput input = new IssueInputBuilder().setDescription(issueDescription).build();
        restClient.getIssueClient().updateIssue(issueKey, input).claim();
        System.out.println("✅ Issue updated successfully: " + issueKey);
    }

    @Tool(name = "addComment", description = "Add a comment to a Jira issue")
    public void addComment(
            @ToolParam(description = "Key of the issue to comment on", required=true) String issueKey,
            @ToolParam(description = "Comment text to add", required=true) String commentBody
    ) throws Exception {
        Issue issue = restClient.getIssueClient().getIssue(issueKey).claim();
        restClient.getIssueClient().addComment(issue.getCommentsUri(), Comment.valueOf(commentBody));
        System.out.println("✅ Comment added to issue: " + issueKey);
    }

    @Tool(name = "getComments", description = "Fetch all comments from an issue")
    public List<String> getComments(
            @ToolParam(description = "Key of the Jira issue", required=true) String issueKey
    ) throws Exception {
        return StreamSupport.stream(getIssueDetails(issueKey).getComments().spliterator(), false)
                .map(Comment::getBody)
                .collect(Collectors.toList());
    }

    @Tool(name = "deleteIssue", description = "Delete a Jira issue by key")
    public void deleteIssue(
            @ToolParam(description = "Jira issue key to delete", required=true) String issueKey,
            @ToolParam(description = "If true, deletes subtasks too", required=true) boolean deleteSubTasks
    ) throws Exception {
        restClient.getIssueClient().deleteIssue(issueKey, deleteSubTasks).claim();
        System.out.println("🗑️ Issue deleted: " + issueKey);
    }

    @Tool(name = "getTotalVotesCount", description = "Get total number of votes on a Jira issue")
    public int getTotalVotesCount(
            @ToolParam(description = "Key of the Jira issue", required=true) String issueKey
    ) throws Exception {
        BasicVotes votes = getIssueDetails(issueKey).getVotes();
        return votes == null ? 0 : votes.getVotes();
    }

    // ===============================
    // 🧩 Internal Helper (not exposed)
    // ===============================

    private BasicUser createUser(String displayName) throws Exception {
        String array = searchUserByDisplayName(displayName);
        JSONArray jsonArray = new JSONArray(array);
        if (jsonArray.length() == 0) {
            System.out.println("No user found for display name: " + displayName);
            return null;
        }

        JSONObject user = jsonArray.getJSONObject(0);
        String self = user.getString("self");
        String accountId = user.getString("accountId");

        System.out.println("✅ Found user " + displayName + " (" + accountId + ")");
        return new BasicUser(URI.create(self), accountId, displayName);
    }

    public String searchUserByDisplayName(String displayName) throws Exception {
        String url = clientConfiguration.getBaseUrl() + "/rest/api/3/user/search?query=" +
                URLEncoder.encode(displayName, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", clientConfiguration.getAuthorizationHeader())
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = clientConfiguration.getHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("Failed to search user: " + response.body());
        }
    }
}
