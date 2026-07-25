package com.q4magic.common.salesforce.todo.serviceImpl;

import com.q4magic.common.salesforce.todo.service.SalesforceToDoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service("SalesforceToDoService")
public class SalesforceToDoServiceImpl implements SalesforceToDoService {

    private final RestClient restClient = RestClient.create();

    @Value("${salesforce.api.version:v60.0}")
    private String apiVersion;

    private HttpHeaders authHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Override
    public Map<String, Object> getAllTasks(String accessToken, String instanceUrl) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Describe Task to get all field names
            String describeUrl = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Task/describe";
            ResponseEntity<Map> describeResponse = restTemplate.exchange(
                    describeUrl, HttpMethod.GET, new HttpEntity<>(authHeaders(accessToken)), Map.class);

            List<Map<String, Object>> fields = (List<Map<String, Object>>) describeResponse.getBody().get("fields");
            List<String> fieldNames = new ArrayList<>();
            for (Map<String, Object> f : fields) {
                fieldNames.add((String) f.get("name"));
            }

            // Query all tasks
            String query = "SELECT " + String.join(",", fieldNames) + " FROM Task";
            String url = UriComponentsBuilder
                    .fromHttpUrl(instanceUrl + "/services/data/" + apiVersion + "/query")
                    .queryParam("q", query)
                    .build(false)
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders(accessToken)), Map.class);

            resBody.put("success", true);
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
    public Map<String, Object> getTaskById(String taskId, String accessToken, String instanceUrl) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Task/" + taskId;

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders(accessToken)), Map.class);

            Map<String, Object> resBody = new HashMap<>();
            resBody.put("success", true);
            resBody.put("task", response.getBody());
            return resBody;

        } catch (HttpClientErrorException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("status", e.getStatusCode().value());
            error.put("error", e.getResponseBodyAsString());
            return error;
        }
    }

    @Override
    public Map<String, Object> createTask(Map<String, Object> taskData, String accessToken, String instanceUrl) {
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Task/";
            ResponseEntity<Map> response = new RestTemplate().exchange(
                    url, HttpMethod.POST, new HttpEntity<>(taskData, authHeaders(accessToken)), Map.class);

            Map<String, Object> resBody = new HashMap<>();
            resBody.put("success", response.getStatusCode().is2xxSuccessful());
            resBody.put("status", response.getStatusCode().value());
            resBody.put("task", response.getBody());
            return resBody;

        } catch (HttpClientErrorException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("status", e.getStatusCode().value());
            error.put("message", e.getResponseBodyAsString());
            return error;
        }
    }

    @Override
    public Map<String, Object> updateTask(String taskId, Map<String, Object> taskData, String accessToken, String instanceUrl) {
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Task/" + taskId;
            ResponseEntity<Void> response = restClient.patch()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .body(taskData)
                    .retrieve()
                    .toBodilessEntity();

            Map<String, Object> resBody = new HashMap<>();
            resBody.put("success", response.getStatusCode().is2xxSuccessful());
            resBody.put("status", response.getStatusCode().value());
            return resBody;

        } catch (HttpClientErrorException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("status", e.getStatusCode().value());
            error.put("message", e.getResponseBodyAsString());
            return error;
        }
    }

    @Override
    public Map<String, Object> deleteTask(String taskId, String accessToken, String instanceUrl) {
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Task/" + taskId;
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
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("status", e.getStatusCode().value());
            error.put("message", e.getResponseBodyAsString());
            return error;
        }
    }
}
