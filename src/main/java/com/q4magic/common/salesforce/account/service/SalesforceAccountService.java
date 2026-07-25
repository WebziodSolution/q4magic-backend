package com.q4magic.common.salesforce.account.service;

import java.util.Map;

public interface SalesforceAccountService {
    Map<String, Object> getAllAccounts(String accessToken, String instanceUrl);

    Map<String, Object> getAccountDetails(String accountId, String accessToken, String instanceUrl);

    Map<String, Object> createAccount(Map<String, Object> accountData, String accessToken, String instanceUrl);

    Map<String, Object> updateAccount(String accountId, Map<String, Object> accountData, String accessToken, String instanceUrl);

    Map<String, Object> deleteAccount(String accountId, String accessToken, String instanceUrl);
}
