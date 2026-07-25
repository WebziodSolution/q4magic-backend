package com.q4magic.common.salesforce.account.serviceImpl;

import com.q4magic.common.salesforce.account.service.SalesforceAccountService;
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

@Service(value = "SalesforceAccountService")
public class SalesforceAccountServiceImpl implements SalesforceAccountService {

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

    @Override
    public Map<String, Object> getAllAccounts(String accessToken, String instanceUrl) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. Call describe for Account
            String describeUrl = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Account/describe";
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

            // 2. SOQL query with all fields
            String query = "SELECT " + String.join(",", fieldNames) + " FROM Account";

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

            // 4. Return fields + records
            resBody.put("fields", fieldNames);
            resBody.put("records", response.getBody().get("records"));
            return resBody;

        } catch (HttpClientErrorException e) {
            // Salesforce 401, 403, etc.
            resBody.put("success", false);
            resBody.put("status", e.getStatusCode().value());
            resBody.put("error", e.getStatusText());
            resBody.put("message", e.getResponseBodyAsString()); // Salesforce error details
            return resBody;

        } catch (Exception e) {
            // Generic error
            resBody.put("success", false);
            resBody.put("status", 500);
            resBody.put("error", e.getClass().getSimpleName());
            resBody.put("message", e.getMessage());
            return resBody;
        }
    }

    @Override
    public Map<String, Object> getAccountDetails(String accountId, String accessToken, String instanceUrl) {
        try {
            Map<String, Object> resBody = new HashMap<>();

            // ✅ Use SObject REST API instead of SOQL
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Account/" + accountId;

            ResponseEntity<Map> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(accessToken)),
                    Map.class
            );

            resBody.put("account", response.getBody());
            return resBody;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> createAccount(Map<String, Object> accountData, String accessToken, String instanceUrl) {
        try {
            Map<String, Object> resBody = new HashMap<>();
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Account/";

            ResponseEntity<Map> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(accountData, authHeaders(accessToken)),
                    Map.class
            );

            boolean success = response.getStatusCode().is2xxSuccessful();
            resBody.put("success", success);
            resBody.put("status", response.getStatusCode().value());

            if (success) {
                resBody.put("account", response.getBody());
            } else {
                resBody.put("error", true);
                resBody.put("errorMessage", response.getBody());
            }

            return resBody;
        } catch (HttpClientErrorException e) {
            // Salesforce returns JSON array of errors on 4xx
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
    public Map<String, Object> updateAccount(String accountId, Map<String, Object> accountData, String accessToken, String instanceUrl) {
        try {
            Map<String, Object> resBody = new HashMap<>();
            // CORRECT URL: Remove the "_all" suffix if it's still being appended
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Account/" + accountId;

            ResponseEntity<Void> response = restClient.patch()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .body(accountData)
                    .retrieve()
                    .toBodilessEntity(); // For responses with no body content

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
        } catch (ResourceAccessException e) { // Catch ResourceAccessException specifically for I/O errors
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
    public Map<String, Object> deleteAccount(String accountId, String accessToken, String instanceUrl) {
        try {
            Map<String, Object> resBody = new HashMap<>();
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Account/" + accountId;

            ResponseEntity<Void> response = restClient.delete() // Use restClient.put() for PUT requests
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .retrieve()
                    .toBodilessEntity(); // For responses with no body content

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
