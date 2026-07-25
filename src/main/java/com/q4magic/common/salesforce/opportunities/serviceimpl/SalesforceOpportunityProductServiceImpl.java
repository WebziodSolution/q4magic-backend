package com.q4magic.common.salesforce.opportunities.serviceimpl;

import com.q4magic.common.salesforce.opportunities.service.SalesforceOpportunityProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service(value = "SalesforceOpportunityProductService")
public class SalesforceOpportunityProductServiceImpl implements SalesforceOpportunityProductService {

    @Value("${salesforce.api.version}")
    private String apiVersion;

    @Autowired
    private RestClient restClient;

    @Override
    public Map<String, Object> getAllOpportunityProductsByOppId(String accessToken,
                                                                String instanceUrl,
                                                                String opportunityId) {
        Map<String, Object> resBody = new HashMap<>();

        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1️⃣ Describe OpportunityLineItem to get all fields
            String describeUrl = instanceUrl + "/services/data/" + apiVersion + "/sobjects/OpportunityLineItem/describe";

            ResponseEntity<Map> describeResponse = restTemplate.exchange(
                    describeUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(accessToken)),
                    Map.class
            );

            List<Map<String, Object>> fields =
                    (List<Map<String, Object>>) describeResponse.getBody().get("fields");

            List<String> fieldNames = fields.stream()
                    .map(f -> (String) f.get("name"))
                    .collect(Collectors.toList());

            // 2️⃣ Build SOQL with WHERE OpportunityId = '...'
            String query = "SELECT " + String.join(",", fieldNames) +
                    " FROM OpportunityLineItem" +
                    " WHERE OpportunityId = '" + opportunityId + "'";

            String queryUrl = UriComponentsBuilder
                    .fromHttpUrl(instanceUrl + "/services/data/" + apiVersion + "/query")
                    .queryParam("q", query)
                    .build(false)   // don't encode '=' and spaces; SF expects standard SOQL
                    .toUriString();

            // 3️⃣ Execute query
            ResponseEntity<Map> queryResponse = restTemplate.exchange(
                    queryUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(accessToken)),
                    Map.class
            );

            // 4️⃣ Prepare response
            resBody.put("success", true);
            resBody.put("fields", fieldNames);
            resBody.put("records", queryResponse.getBody().get("records"));
            resBody.put("totalSize", queryResponse.getBody().get("totalSize"));
            resBody.put("done", queryResponse.getBody().get("done"));

            return resBody;

        } catch (HttpClientErrorException e) {
            resBody.put("success", false);
            resBody.put("status", e.getStatusCode().value());
            resBody.put("error", e.getStatusText());
            resBody.put("message", e.getResponseBodyAsString());
            return resBody;

        } catch (Exception e) {
            resBody.put("success", false);
            resBody.put("status", 500);
            resBody.put("error", e.getClass().getSimpleName());
            resBody.put("message", e.getMessage());
            return resBody;
        }
    }


    @Override
    public Map<String, Object> createOpportunityProduct(String accessToken,
                                                        String instanceUrl,
                                                        Map<String, Object> productData) {
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/OpportunityLineItem/";

            ResponseEntity<Map> response = restClient.post()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))   // ✅ add Bearer here
                    .body(productData)                                              // ✅ raw body, not HttpEntity
                    .retrieve()
                    .toEntity(Map.class);

            Map<String, Object> res = new HashMap<>();
            res.put("success", response.getStatusCode().is2xxSuccessful());
            res.put("status", response.getStatusCode().value());
            res.put("result", response.getBody());
            return res;

        } catch (HttpClientErrorException e) {
            return handleError(e);
        } catch (Exception e) {
            return handleGenericError(e);
        }
    }

    @Override
    public Map<String, Object> updateOpportunityProduct(String accessToken,
                                                        String instanceUrl,
                                                        String productId,
                                                        Map<String, Object> productData) {
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/OpportunityLineItem/" + productId;

            ResponseEntity<Void> response = restClient.patch()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))   // ✅
                    .body(productData)                                              // ✅
                    .retrieve()
                    .toBodilessEntity();

            Map<String, Object> res = new HashMap<>();
            res.put("success", response.getStatusCode().is2xxSuccessful());
            res.put("status", response.getStatusCode().value());
            return res;

        } catch (HttpClientErrorException e) {
            return handleError(e);
        } catch (Exception e) {
            return handleGenericError(e);
        }
    }

    @Override
    public Map<String, Object> deleteOpportunityProduct(String accessToken,
                                                        String instanceUrl,
                                                        String productId) {
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/OpportunityLineItem/" + productId;

            ResponseEntity<Void> response = restClient.delete()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))   // ✅
                    .retrieve()
                    .toBodilessEntity();

            Map<String, Object> res = new HashMap<>();
            res.put("success", response.getStatusCode().is2xxSuccessful());
            res.put("status", response.getStatusCode().value());
            return res;

        } catch (HttpClientErrorException e) {
            return handleError(e);
        } catch (Exception e) {
            return handleGenericError(e);
        }
    }


    /**
     * Ensure the Opportunity has a Pricebook2Id and return the PricebookEntryId for the given Product2Id.
     * If the Opportunity has no Price Book, it will set the Standard Price Book.
     */

    @Override
    public String ensureOpportunityPricebookAndGetEntryId(
            String accessToken,
            String instanceUrl,
            String opportunityId,
            String product2Id
    ) {
        try {
            String queryUrl = instanceUrl + "/services/data/" + apiVersion + "/query";

            // 1️⃣ Get Opportunity (plain SOQL, no URL-encoding here)
            String oppSoql = "SELECT Id, Pricebook2Id FROM Opportunity WHERE Id = '" + opportunityId + "'";

            UriComponents oppUri = UriComponentsBuilder
                    .fromHttpUrl(queryUrl)
                    .queryParam("q", oppSoql)
                    .build()
                    .encode();

            ResponseEntity<Map> oppResponse = restClient.get()
                    .uri(oppUri.toUri())
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .retrieve()
                    .toEntity(Map.class);

            List<Map<String, Object>> oppRecords =
                    (List<Map<String, Object>>) oppResponse.getBody().get("records");

            if (oppRecords == null || oppRecords.isEmpty()) {
                throw new RuntimeException("Opportunity not found in Salesforce: " + opportunityId);
            }

            Map<String, Object> opp = oppRecords.get(0);
            String pricebook2Id = (String) opp.get("Pricebook2Id");

            // 2️⃣ If no Pricebook2Id, choose a default Pricebook2
            if (pricebook2Id == null) {
                // 2.a Try Standard + active
                String pbStandardSoql =
                        "SELECT Id FROM Pricebook2 WHERE IsStandard = true AND IsActive = true LIMIT 1";

                UriComponents pbStandardUri = UriComponentsBuilder
                        .fromHttpUrl(queryUrl)
                        .queryParam("q", pbStandardSoql)
                        .build()
                        .encode();

                ResponseEntity<Map> pbStandardResponse = restClient.get()
                        .uri(pbStandardUri.toUri())
                        .headers(headers -> headers.addAll(authHeaders(accessToken)))
                        .retrieve()
                        .toEntity(Map.class);

                List<Map<String, Object>> pbStandardRecords =
                        (List<Map<String, Object>>) pbStandardResponse.getBody().get("records");

                if (pbStandardRecords != null && !pbStandardRecords.isEmpty()) {
                    pricebook2Id = (String) pbStandardRecords.get(0).get("Id");
                } else {
                    // 2.b Fallback: any active pricebook
                    String pbAnySoql =
                            "SELECT Id FROM Pricebook2 WHERE IsActive = true LIMIT 1";

                    UriComponents pbAnyUri = UriComponentsBuilder
                            .fromHttpUrl(queryUrl)
                            .queryParam("q", pbAnySoql)
                            .build()
                            .encode();

                    ResponseEntity<Map> pbAnyResponse = restClient.get()
                            .uri(pbAnyUri.toUri())
                            .headers(headers -> headers.addAll(authHeaders(accessToken)))
                            .retrieve()
                            .toEntity(Map.class);

                    List<Map<String, Object>> pbAnyRecords =
                            (List<Map<String, Object>>) pbAnyResponse.getBody().get("records");

                    if (pbAnyRecords != null && !pbAnyRecords.isEmpty()) {
                        pricebook2Id = (String) pbAnyRecords.get(0).get("Id");
                    } else {
                        // ❌ Absolutely no active price book in this org
                        throw new RuntimeException(
                                "No active Price Book found in Salesforce. " +
                                        "Create or activate at least one Price Book before syncing opportunity products."
                        );
                    }
                }

                // PATCH Opportunity with chosen Pricebook2Id
                String oppUrl = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Opportunity/" + opportunityId;

                Map<String, Object> oppUpdate = new HashMap<>();
                oppUpdate.put("Pricebook2Id", pricebook2Id);

                restClient.patch()
                        .uri(oppUrl)
                        .headers(headers -> headers.addAll(authHeaders(accessToken)))
                        .body(oppUpdate)
                        .retrieve()
                        .toBodilessEntity();
            }

            // 3️⃣ Get PricebookEntry for this Product in that Pricebook
            String pbeSoql =
                    "SELECT Id FROM PricebookEntry " +
                            "WHERE Pricebook2Id = '" + pricebook2Id + "' " +
                            "AND Product2Id = '" + product2Id + "' " +
                            "AND IsActive = true " +
                            "LIMIT 1";

            UriComponents pbeUri = UriComponentsBuilder
                    .fromHttpUrl(queryUrl)
                    .queryParam("q", pbeSoql)
                    .build()
                    .encode();

            ResponseEntity<Map> pbeResponse = restClient.get()
                    .uri(pbeUri.toUri())
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .retrieve()
                    .toEntity(Map.class);

            List<Map<String, Object>> pbeRecords =
                    (List<Map<String, Object>>) pbeResponse.getBody().get("records");

            if (pbeRecords == null || pbeRecords.isEmpty()) {
                throw new RuntimeException(
                        "No active PricebookEntry found for Product " + product2Id +
                                " in Pricebook " + pricebook2Id +
                                ". Ensure the product is synced to Salesforce and added to this price book."
                );
            }

            return (String) pbeRecords.get(0).get("Id");

        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed in ensureOpportunityPricebookAndGetEntryId: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed in ensureOpportunityPricebookAndGetEntryId: " + e.getMessage(), e);
        }
    }



    // ===== helper methods (same style as your Partner service) =====

    private HttpHeaders authHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }

    private Map<String, Object> handleError(HttpClientErrorException e) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("status", e.getStatusCode().value());
        res.put("error", true);
        res.put("errorMessage", e.getResponseBodyAsString());
        return res;
    }

    private Map<String, Object> handleGenericError(Exception e) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("status", 500);
        res.put("error", e.getClass().getSimpleName());
        res.put("errorMessage", e.getMessage());
        return res;
    }
}
