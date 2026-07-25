package com.q4magic.common.salesforce.competitor.serviceImpl;

import com.q4magic.common.salesforce.competitor.service.SalesforceCompetitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service(value = "salesforceCompetitorService")
public class SalesforceCompetitorServiceImpl implements SalesforceCompetitorService {

    @Autowired
    private RestClient restClient;

    @Value("${salesforce.api.version:v60.0}")
    private String apiVersion;

    private HttpHeaders authHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Override
    public Map<String, Object> getAllOpportunityCompetitors(String accessToken, String instanceUrl) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. Describe the OpportunityCompetitor object to get all fields
            String describeUrl = instanceUrl + "/services/data/" + apiVersion + "/sobjects/OpportunityCompetitor/describe";
            ResponseEntity<Map> describeResponse = restTemplate.exchange(
                    describeUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(accessToken)),
                    Map.class
            );

            List<Map<String, Object>> fields = (List<Map<String, Object>>) describeResponse.getBody().get("fields");
            List<String> fieldNames = fields.stream()
                    .map(f -> (String) f.get("name"))
                    .collect(Collectors.toList());

            // 2. Build SOQL query for all records
            String query = "SELECT " + String.join(",", fieldNames) + " FROM OpportunityCompetitor";

            String url = UriComponentsBuilder
                    .fromHttpUrl(instanceUrl + "/services/data/" + apiVersion + "/query")
                    .queryParam("q", query)
                    .build(false)
                    .toUriString();

            // 3. Execute query
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(accessToken)),
                    Map.class
            );

            // 4. Prepare response
            resBody.put("records", response.getBody().get("records"));
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
    public Map<String, Object> getOpportunityCompetitorDetails(String competitorId, String accessToken, String instanceUrl) {
        try {
            Map<String, Object> resBody = new HashMap<>();
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/OpportunityCompetitor/" + competitorId;

            ResponseEntity<Map> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(accessToken)),
                    Map.class
            );

            resBody.put("opportunityCompetitor", response.getBody());
            return resBody;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> createOpportunityCompetitor(Map<String, Object> data, String accessToken, String instanceUrl) {
        try {
            Map<String, Object> resBody = new HashMap<>();
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/OpportunityCompetitor/";

            ResponseEntity<Map> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(data, authHeaders(accessToken)),
                    Map.class
            );

            boolean success = response.getStatusCode().is2xxSuccessful();
            resBody.put("success", success);
            resBody.put("status", response.getStatusCode().value());

            if (success) {
                resBody.put("opportunityCompetitor", response.getBody());
            } else {
                resBody.put("error", true);
                resBody.put("errorMessage", response.getBody());
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
    public Map<String, Object> updateOpportunityCompetitor(String competitorId, Map<String, Object> data, String accessToken, String instanceUrl) {
        try {
            Map<String, Object> resBody = new HashMap<>();
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/OpportunityCompetitor/" + competitorId;

            ResponseEntity<Void> response = restClient.patch()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .body(data)
                    .retrieve()
                    .toBodilessEntity();

            boolean success = response.getStatusCode().is2xxSuccessful();
            resBody.put("success", success);
            resBody.put("status", response.getStatusCode().value());
            return resBody;

        } catch (HttpClientErrorException e) {
            Map<String, Object> resBody = new HashMap<>();
            resBody.put("success", false);
            resBody.put("status", e.getStatusCode().value());
            resBody.put("error", true);
            resBody.put("errorMessage", e.getResponseBodyAsString());
            return resBody;

        } catch (ResourceAccessException e) {
            e.printStackTrace();
            Map<String, Object> resBody = new HashMap<>();
            resBody.put("success", false);
            resBody.put("error", true);
            resBody.put("errorMessage", "I/O Error: " + e.getMessage());
            return resBody;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> deleteOpportunityCompetitor(String competitorId, String accessToken, String instanceUrl) {
        try {
            Map<String, Object> resBody = new HashMap<>();
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/OpportunityCompetitor/" + competitorId;

            ResponseEntity<Void> response = restClient.delete()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .retrieve()
                    .toBodilessEntity();

            boolean success = response.getStatusCode().is2xxSuccessful();
            resBody.put("success", success);
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
}