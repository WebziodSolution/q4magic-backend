package com.q4magic.common.salesforce.syncFromQ4Magic;

import java.util.Map;

public interface SyncFromQ4MagicService {
    Map<String, Object> syncAccounts(String accessToken, String instanceUrl, Integer userId);

    Map<String, Object> syncOpportunity(String accessToken, String instanceUrl, Integer userId);

    Map<String, Object> syncContacts(String accessToken, String instanceUrl, Integer userId);

    Map<String, Object> syncOpportunityPartner(String accessToken, String instanceUrl, Integer userId);

    Map<String, Object> syncOpportunityContacts(String accessToken, String instanceUrl, Integer userId);

    Map<String, Object> syncOpportunityProducts(String accessToken, String instanceUrl, Integer userId);
//    Map<String, Object> syncTodo(String accessToken, String instanceUrl, Integer userId);

}
