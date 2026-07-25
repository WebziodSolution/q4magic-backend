package com.q4magic.common.salesforce.activityHistory.service;

import java.util.Map;

public interface SalesforceActivityHistoryService {
    Map<String, Object> getAllActivityHistory(String accessToken, String instanceUrl);

    Map<String, Object> getActivityHistoryById(String accessToken, String instanceUrl, String activityId);

    Map<String, Object> createActivityHistory(String accessToken, String instanceUrl, Map<String, Object> activityData);

    Map<String, Object> updateActivityHistory(String accessToken, String instanceUrl, String activityId, Map<String, Object> activityData);

    Map<String, Object> deleteActivityHistory(String accessToken, String instanceUrl, String activityId);
}
