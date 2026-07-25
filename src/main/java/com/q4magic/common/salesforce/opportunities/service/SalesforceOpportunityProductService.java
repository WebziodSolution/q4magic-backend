package com.q4magic.common.salesforce.opportunities.service;

import java.util.Map;

public interface SalesforceOpportunityProductService {

    Map<String, Object> getAllOpportunityProductsByOppId(String accessToken,
                                                   String instanceUrl,
                                                   String opportunityId);
    /**
     * Create a new Opportunity Product (OpportunityLineItem) in Salesforce.
     *
     * @param accessToken  OAuth token
     * @param instanceUrl  Salesforce instance URL
     * @param productData  Map of field API names -> values
     */
    Map<String, Object> createOpportunityProduct(String accessToken,
                                                 String instanceUrl,
                                                 Map<String, Object> productData);

    /**
     * Update an existing Opportunity Product (OpportunityLineItem) in Salesforce.
     *
     * @param accessToken  OAuth token
     * @param instanceUrl  Salesforce instance URL
     * @param productId    Id of OpportunityLineItem
     * @param productData  Map of field API names -> values
     */
    Map<String, Object> updateOpportunityProduct(String accessToken,
                                                 String instanceUrl,
                                                 String productId,
                                                 Map<String, Object> productData);

    /**
     * Delete an existing Opportunity Product (OpportunityLineItem) in Salesforce.
     *
     * @param accessToken  OAuth token
     * @param instanceUrl  Salesforce instance URL
     * @param productId    Id of OpportunityLineItem
     */
    Map<String, Object> deleteOpportunityProduct(String accessToken,
                                                 String instanceUrl,
                                                 String productId);

    String ensureOpportunityPricebookAndGetEntryId(
            String accessToken,
            String instanceUrl,
            String opportunityId,
            String product2Id
    );
}
