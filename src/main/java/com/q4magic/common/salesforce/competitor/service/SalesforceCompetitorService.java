package com.q4magic.common.salesforce.competitor.service;

import java.util.Map;

public interface SalesforceCompetitorService {

    Map<String, Object> getAllOpportunityCompetitors(String accessToken, String instanceUrl);

    Map<String, Object> getOpportunityCompetitorDetails(String competitorId, String accessToken, String instanceUrl);

    Map<String, Object> createOpportunityCompetitor(Map<String, Object> data, String accessToken, String instanceUrl);

    Map<String, Object> updateOpportunityCompetitor(String competitorId, Map<String, Object> data, String accessToken, String instanceUrl);

    Map<String, Object> deleteOpportunityCompetitor(String competitorId, String accessToken, String instanceUrl);
}

