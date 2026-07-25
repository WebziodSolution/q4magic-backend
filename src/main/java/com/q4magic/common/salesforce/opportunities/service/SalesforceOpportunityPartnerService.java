package com.q4magic.common.salesforce.opportunities.service;

import java.util.Map;

public interface SalesforceOpportunityPartnerService {
    Map<String, Object> createOpportunityPartner(String accessToken, String instanceUrl, Map<String, Object> partnerData);

    Map<String, Object> updateOpportunityPartner(String accessToken, String instanceUrl, String partnerId, Map<String, Object> partnerData);

    Map<String, Object> deleteOpportunityPartner(String accessToken, String instanceUrl, String partnerId);
}