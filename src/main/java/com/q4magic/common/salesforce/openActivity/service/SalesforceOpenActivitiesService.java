package com.q4magic.common.salesforce.openActivity.service;

import java.util.Map;

public interface SalesforceOpenActivitiesService {
    Map<String, Object> getAllOpenActivities(String accessToken, String instanceUrl);

    Map<String, Object> getOpenActivityById(String accessToken, String instanceUrl, String activityId);

    Map<String, Object> createOpenActivity(String accessToken, String instanceUrl, Map<String, Object> activityData);

    Map<String, Object> updateOpenActivity(String accessToken, String instanceUrl, String activityId, Map<String, Object> activityData);

    Map<String, Object> deleteOpenActivity(String accessToken, String instanceUrl, String activityId);
}

