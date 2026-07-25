package com.q4magic.common.salesforce.activityHistory.serviceImpl;


import com.q4magic.common.salesforce.activityHistory.service.SalesforceActivityHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service(value = "salesforceActivityHistoryService")
public class SalesforceActivityHistoryServiceImpl implements SalesforceActivityHistoryService {

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
    public Map<String, Object> getAllActivityHistory(String accessToken, String instanceUrl) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Query Activity History related to Accounts
            String query = "SELECT Id, Name, (SELECT Id, Subject, ActivityDate, Status, OwnerId FROM ActivityHistories) FROM Account";

            String queryUrl = UriComponentsBuilder
                    .fromHttpUrl(instanceUrl + "/services/data/" + apiVersion + "/query")
                    .queryParam("q", query)
                    .build(false)
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.exchange(
                    queryUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(accessToken)),
                    Map.class
            );

            resBody.put("success", true);
            resBody.put("records", response.getBody().get("records"));
            return resBody;

        } catch (HttpClientErrorException e) {
            resBody.put("success", false);
            resBody.put("error", e.getStatusText());
            resBody.put("message", e.getResponseBodyAsString());
            resBody.put("status", e.getStatusCode().value());
            return resBody;
        } catch (Exception e) {
            resBody.put("success", false);
            resBody.put("error", e.getClass().getSimpleName());
            resBody.put("message", e.getMessage());
            resBody.put("status", 500);
            return resBody;
        }
    }

    @Override
    public Map<String, Object> getActivityHistoryById(String accessToken, String instanceUrl, String activityId) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Task/" + activityId;
            ResponseEntity<Map> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(accessToken)),
                    Map.class
            );
            resBody.put("success", true);
            resBody.put("activityHistory", response.getBody());
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
    public Map<String, Object> createActivityHistory(String accessToken, String instanceUrl, Map<String, Object> activityData) {
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/ActivityHistory/";
            ResponseEntity<Map> response = restClient.post()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .body(activityData)
                    .retrieve()
                    .toEntity(Map.class);

            Map<String, Object> resBody = new HashMap<>();
            resBody.put("success", response.getStatusCode().is2xxSuccessful());
            resBody.put("status", response.getStatusCode().value());
            resBody.put("activityHistory", response.getBody());
            return resBody;
        } catch (HttpClientErrorException e) {
            Map<String, Object> resBody = new HashMap<>();
            resBody.put("success", false);
            resBody.put("status", e.getStatusCode().value());
            resBody.put("errorMessage", e.getResponseBodyAsString());
            return resBody;
        }
    }

    @Override
    public Map<String, Object> updateActivityHistory(String accessToken, String instanceUrl, String activityId, Map<String, Object> activityData) {
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/ActivityHistory/" + activityId;
            ResponseEntity<Void> response = restClient.patch()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .body(activityData)
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
            resBody.put("errorMessage", e.getResponseBodyAsString());
            return resBody;
        }
    }

    @Override
    public Map<String, Object> deleteActivityHistory(String accessToken, String instanceUrl, String activityId) {
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/ActivityHistory/" + activityId;
            ResponseEntity<Void> response = restClient.delete()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
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
            resBody.put("errorMessage", e.getResponseBodyAsString());
            return resBody;
        }
    }
}