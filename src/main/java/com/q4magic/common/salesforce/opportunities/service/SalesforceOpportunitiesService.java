package com.q4magic.common.salesforce.opportunities.service;

import java.util.Map;

public interface SalesforceOpportunitiesService {
    Map<String, Object> getAllOpportunities(String accessToken, String instanceUrl);

    Map<String, Object> getOpportunityById(String accessToken, String instanceUrl, String opportunityId);

    Map<String, Object> createOpportunity(String accessToken, String instanceUrl, Map<String, Object> opportunityData);

    Map<String, Object> updateOpportunity(String accessToken, String instanceUrl, String opportunityId, Map<String, Object> opportunityData);

    Map<String, Object> deleteOpportunity(String accessToken, String instanceUrl, String opportunityId);

    Map<String, Object> listOpportunityContacts(String instanceUrl, String accessToken, String opportunityId, Integer limit, String nextRecordsUrl);

    Map<String, Object> createOpportunityContact(String instanceUrl, String accessToken, Map<String, Object> data);

    Map<String, Object> updateOpportunityContact(String instanceUrl, String accessToken, String ocrId, Map<String, Object> fields);

    Map<String, Object> deleteOpportunityContact(String instanceUrl, String accessToken, String ocrId);

    Map<String, Object> getOpportunityContact(String instanceUrl, String accessToken, String ocrId);

}
