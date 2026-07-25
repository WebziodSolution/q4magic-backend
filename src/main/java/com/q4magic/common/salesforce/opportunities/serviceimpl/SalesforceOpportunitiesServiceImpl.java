package com.q4magic.common.salesforce.opportunities.serviceimpl;

import com.q4magic.common.salesforce.opportunities.service.SalesforceOpportunitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service(value = "salesforceOpportunitiesService")
public class SalesforceOpportunitiesServiceImpl implements SalesforceOpportunitiesService {
    @Autowired
    private RestClient restClient;

    @Autowired
    org.springframework.core.env.Environment env;

    @Value("${salesforce.api.version:v60.0}")
    private String apiVersion;

    private HttpHeaders authHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ---------- helpers ----------
    private String baseUrl(String instanceUrl) {
        return instanceUrl + "/services/data/" + apiVersion;
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    @Override
    public Map<String, Object> getAllOpportunities(String accessToken, String instanceUrl) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Step 1: Describe Opportunity fields
            String describeUrl = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Opportunity/describe";
            ResponseEntity<Map> describeResponse = restTemplate.exchange(
                    describeUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(accessToken)),
                    Map.class
            );

            // Extract Opportunity fields
            List<Map<String, Object>> fields = (List<Map<String, Object>>) describeResponse.getBody().get("fields");
            List<String> fieldNames = fields.stream()
                    .map(f -> (String) f.get("name"))
                    .collect(Collectors.toList());

            // Step 2: Find correct child relationship name for OpportunityPartner
            List<Map<String, Object>> childRelationships =
                    (List<Map<String, Object>>) describeResponse.getBody().get("childRelationships");

            String partnerRelationshipName = null;
            for (Map<String, Object> child : childRelationships) {
                String childSObject = (String) child.get("childSObject");
                if ("OpportunityPartner".equals(childSObject)) {
                    partnerRelationshipName = (String) child.get("relationshipName");
                    break;
                }
            }

            if (partnerRelationshipName == null) {
                throw new RuntimeException("Could not find child relationship name for OpportunityPartner");
            }

            // Step 3: Build SOQL query dynamically
            String partnerQuery = "SELECT " + String.join(",", fieldNames)
                    + ", (SELECT Id, AccountToId, Role, IsPrimary FROM " + partnerRelationshipName + ") "
                    + "FROM Opportunity";

            String partnerUrl = UriComponentsBuilder
                    .fromHttpUrl(instanceUrl + "/services/data/" + apiVersion + "/query")
                    .queryParam("q", partnerQuery)
                    .build(false)
                    .toUriString();

            ResponseEntity<Map> partnerResponse = restTemplate.exchange(
                    partnerUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(accessToken)),
                    Map.class
            );

            // Step 4: Flatten OpportunityPartner records
            List<Map<String, Object>> opportunities = (List<Map<String, Object>>) partnerResponse.getBody().get("records");
            List<Map<String, Object>> allPartners = new ArrayList<>();
            for (Map<String, Object> opp : opportunities) {
                Map<String, Object> partnersWrapper = (Map<String, Object>) opp.get(partnerRelationshipName);
                if (partnersWrapper != null) {
                    List<Map<String, Object>> partners = (List<Map<String, Object>>) partnersWrapper.get("records");
                    if (partners != null) {
                        for (Map<String, Object> partner : partners) {
                            partner.put("OpportunityId", opp.get("Id"));
                            partner.put("OpportunityName", opp.get("Name"));
                            allPartners.add(partner);
                        }
                    }
                }
            }

            // Step 5: Return Opportunities and flattened Partners
            resBody.put("fields", fieldNames);
            resBody.put("opportunities", opportunities);
            resBody.put("partners", allPartners);
            resBody.put("success", true);
            return resBody;

        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            resBody.put("success", false);
            resBody.put("status", e.getStatusCode().value());
            resBody.put("error", e.getStatusText());
            resBody.put("message", e.getResponseBodyAsString());
            return resBody;

        } catch (Exception e) {
            e.printStackTrace();
            resBody.put("success", false);
            resBody.put("status", 500);
            resBody.put("error", e.getClass().getSimpleName());
            resBody.put("message", e.getMessage());
            return resBody;
        }
    }

    @Override
    public Map<String, Object> getOpportunityById(String accessToken, String instanceUrl, String opportunityId) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Opportunity/" + opportunityId;

            ResponseEntity<Map> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(accessToken)),
                    Map.class
            );
            resBody.put("success", true);
            resBody.put("opportunity", response.getBody());
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
    public Map<String, Object> createOpportunity(String accessToken, String instanceUrl, Map<String, Object> opportunityData) {
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Opportunity/";

            ResponseEntity<Map> response = restClient.post()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .body(opportunityData)
                    .retrieve()
                    .toEntity(Map.class); // Returns a Map with the new ID

            Map<String, Object> resBody = new HashMap<>();
            boolean success = response.getStatusCode().is2xxSuccessful();
            resBody.put("success", success);
            resBody.put("status", response.getStatusCode().value());
            if (success) {
                resBody.put("opportunity", response.getBody()); // Get the new Opportunity ID
            }
            return resBody;
        } catch (HttpClientErrorException e) {
            Map<String, Object> resBody = new HashMap<>();
            resBody.put("success", false);
            resBody.put("status", e.getStatusCode().value());
            resBody.put("error", true);
            resBody.put("errorMessage", e.getResponseBodyAsString());
            return resBody;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> updateOpportunity(String accessToken, String instanceUrl, String opportunityId, Map<String, Object> opportunityData) {
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Opportunity/" + opportunityId;

            ResponseEntity<Void> response = restClient.patch()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .body(opportunityData)
                    .retrieve()
                    .toBodilessEntity();

            Map<String, Object> resBody = new HashMap<>();
            resBody.put("success", response.getStatusCode().is2xxSuccessful());
            resBody.put("status", response.getStatusCode().value());
            return resBody;
        } catch (HttpClientErrorException e) {
            Map<String, Object> resBody = new HashMap<>();
            resBody.put("success", false);
            resBody.put("status", e.getStatusCode().value());
            resBody.put("error", true);
            resBody.put("errorMessage", e.getResponseBodyAsString());
            return resBody;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> deleteOpportunity(String accessToken, String instanceUrl, String opportunityId) {
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Opportunity/" + opportunityId;

            ResponseEntity<Void> response = restClient.delete()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .retrieve()
                    .toBodilessEntity(); // Expects no body

            Map<String, Object> resBody = new HashMap<>();
            resBody.put("success", response.getStatusCode().is2xxSuccessful());
            resBody.put("status", response.getStatusCode().value());
            return resBody;
        } catch (HttpClientErrorException e) {
            Map<String, Object> resBody = new HashMap<>();
            resBody.put("success", false);
            resBody.put("status", e.getStatusCode().value());
            resBody.put("error", true);
            resBody.put("errorMessage", e.getResponseBodyAsString());
            return resBody;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> listOpportunityContacts(String instanceUrl, String accessToken,
                                                       String opportunityId, Integer limit, String nextRecordsUrl) {
        try {
            String url;

            if (nextRecordsUrl != null && !nextRecordsUrl.isBlank()) {
                // nextRecordsUrl comes from Salesforce already correctly encoded
                url = instanceUrl + nextRecordsUrl;
            } else {
                String soql = "SELECT Id, OpportunityId, ContactId, Role, IsPrimary, CreatedDate, LastModifiedDate " +
                        "FROM OpportunityContactRole WHERE OpportunityId = '" + opportunityId + "'";

                if (limit != null && limit > 0) {
                    soql += " LIMIT " + limit;
                }else{
                    soql += " LIMIT " + 100;
                }

                // ✅ Let UriComponentsBuilder handle encoding
                url = UriComponentsBuilder
                        .fromHttpUrl(baseUrl(instanceUrl) + "/query")
                        .queryParam("q", soql)
                        .build()
                        .toUriString();
            }

            ResponseEntity<Map> res = restClient.get()
                    .uri(url)
                    .headers(h -> h.addAll(authHeaders(accessToken)))
                    .retrieve()
                    .toEntity(Map.class);

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("status", res.getStatusCode().value());
            body.put("result", res.getBody());
            return body;

        } catch (HttpClientErrorException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("status", e.getStatusCode().value());
            err.put("error", true);
            err.put("errorMessage", e.getResponseBodyAsString());
            return err;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> createOpportunityContact(String instanceUrl, String accessToken,
                                                        Map<String, Object> data) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("OpportunityId", data.get("OpportunityId"));
            payload.put("ContactId", data.get("ContactId"));
            if (data.get("Role") != null) payload.put("Role", data.get("Role"));
            if (data.get("IsPrimary") != null) payload.put("IsPrimary", data.get("IsPrimary"));
            String url = baseUrl(instanceUrl) + "/sobjects/OpportunityContactRole";
            ResponseEntity<Map> res = restClient.post()
                    .uri(url)
                    .headers(h -> {
                        h.addAll(authHeaders(accessToken));
                        h.setContentType(MediaType.APPLICATION_JSON);
                    })
                    .body(payload)
                    .retrieve()
                    .toEntity(Map.class);

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("status", res.getStatusCode().value());
            body.put("result", res.getBody()); // contains id + success flags
            return body;
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("status", e.getStatusCode().value());
            err.put("error", true);
            err.put("errorMessage", e.getResponseBodyAsString());
            return err;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> updateOpportunityContact(String instanceUrl, String accessToken,
                                                        String ocrId, Map<String, Object> fields) {
        try {
            if (fields == null) fields = new HashMap<>();

            // First, verify the record exists
            String queryUrl = baseUrl(instanceUrl) + "/sobjects/OpportunityContactRole/" + ocrId;

            try {
                // Try to get the record first
                restClient.get()
                        .uri(queryUrl)
                        .headers(headers -> headers.addAll(authHeaders(accessToken)))
                        .retrieve()
                        .toBodilessEntity();
            } catch (HttpClientErrorException.NotFound e) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("status", 404);
                err.put("error", true);
                err.put("errorMessage", "OpportunityContactRole record not found with ID: " + ocrId);
                return err;
            }

            String url = baseUrl(instanceUrl) + "/sobjects/OpportunityContactRole/" + ocrId;

            ResponseEntity<Void> response = restClient.patch()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .body(fields)
                    .retrieve()
                    .toBodilessEntity();

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("status", response.getStatusCode().value());
            return body;
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("status", e.getStatusCode().value());
            err.put("error", true);
            err.put("errorMessage", e.getResponseBodyAsString());
            return err;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> deleteOpportunityContact(String instanceUrl, String accessToken, String ocrId) {
        try {
            String url = baseUrl(instanceUrl) + "/sobjects/OpportunityContactRole/" + ocrId;

            ResponseEntity<Void> res = restClient.delete()
                    .uri(url)
                    .headers(h -> h.addAll(authHeaders(accessToken)))
                    .retrieve()
                    .toEntity(Void.class);

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("status", res.getStatusCode().value());
            return body;
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("status", e.getStatusCode().value());
            err.put("error", true);
            err.put("errorMessage", e.getResponseBodyAsString());
            return err;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> getOpportunityContact(String instanceUrl, String accessToken, String ocrId) {
        try {
            String fields = "Id,OpportunityId,ContactId,Role,IsPrimary,CreatedDate,LastModifiedDate";
            String url = baseUrl(instanceUrl) + "/sobjects/OpportunityContactRole/" + ocrId + "?fields=" + encode(fields);

            ResponseEntity<Map> res = restClient.get()
                    .uri(url)
                    .headers(h -> h.addAll(authHeaders(accessToken)))
                    .retrieve()
                    .toEntity(Map.class);

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("status", res.getStatusCode().value());
            body.put("result", res.getBody());
            return body;
        } catch (HttpClientErrorException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("status", e.getStatusCode().value());
            err.put("error", true);
            err.put("errorMessage", e.getResponseBodyAsString());
            return err;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
