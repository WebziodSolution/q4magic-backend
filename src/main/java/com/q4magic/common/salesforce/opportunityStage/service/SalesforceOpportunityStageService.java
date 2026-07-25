package com.q4magic.common.salesforce.opportunityStage.service;

import java.util.Map;

public interface SalesforceOpportunityStageService {
    Map<String, Object> getAllOpportunityStages(String accessToken, String instanceUrl);
}
