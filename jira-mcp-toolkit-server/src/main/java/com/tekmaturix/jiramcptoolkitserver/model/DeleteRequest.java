package com.tekmaturix.jiramcptoolkitserver.model;

public record DeleteRequest(String issueKey, boolean deleteSubTasks) {

}
