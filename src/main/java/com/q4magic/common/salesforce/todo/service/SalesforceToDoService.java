package com.q4magic.common.salesforce.todo.service;

import java.util.Map;

public interface SalesforceToDoService {
    Map<String, Object> getAllTasks(String accessToken, String instanceUrl);

    Map<String, Object> getTaskById(String taskId, String accessToken, String instanceUrl);

    Map<String, Object> createTask(Map<String, Object> taskData, String accessToken, String instanceUrl);

    Map<String, Object> updateTask(String taskId, Map<String, Object> taskData, String accessToken, String instanceUrl);

    Map<String, Object> deleteTask(String taskId, String accessToken, String instanceUrl);
}
