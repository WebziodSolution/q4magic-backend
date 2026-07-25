package com.q4magic.common.salesforce.syncToQ4magic;

import java.util.Map;

public interface SyncToQ4MagicService {
    Map<String, Object> syncAccounts(String assessToken, String instanceUrl, Integer userId);

    Map<String, Object> syncOpportunity(String accessToken, String instanceUrl, Integer userId);

    Map<String, Object> syncOpportunityProducts(String accessToken, String instanceUrl, Integer userId);

    Map<String, Object> syncContacts(String accessToken, String instanceUrl, Integer userId);

//    Map<String, Object> syncTodos(String accessToken, String instanceUrl, Integer userId);

//    Map<String, Object> syncActivity(String accessToken, String instanceUrl, Integer userId);

    Map<String, Object> syncCompetitor(String accessToken, String instanceUrl, Integer userId);

    Map<String, Object> syncStages(String accessToken, String instanceUrl, Integer userId);

    Map<String, Object> syncProducts(String accessToken, String instanceUrl, Integer userId);

    void setContactIdIntoOpp(String accessToken, String instanceUrl, Integer userId);


}
