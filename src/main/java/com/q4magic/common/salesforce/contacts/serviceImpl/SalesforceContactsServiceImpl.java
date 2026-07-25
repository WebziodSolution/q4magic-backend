package com.q4magic.common.salesforce.contacts.serviceImpl;

import com.q4magic.common.salesforce.contacts.service.SalesforceContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service(value = "salesforceContactsService")
public class SalesforceContactsServiceImpl implements SalesforceContactsService {
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
    public Map<String, Object> getAllContacts(String accessToken, String instanceUrl) {
        Map<String, Object> resBody = new HashMap<>();
        // Define your Salesforce API Version (e.g., v60.0)
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. Define fields to select
            LinkedHashSet<String> selected = new LinkedHashSet<>();
            selected.add("Id");
            selected.add("FirstName");
            selected.add("LastName");
            selected.add("Name");
            selected.add("Email");
            selected.add("Phone");
            selected.add("MobilePhone");
            selected.add("AccountId");
            selected.add("Account.Name");

            // 2. Build the raw SOQL string (No manual URLEncoder.encode here!)
            String soql = "SELECT " + String.join(", ", selected)
                    + " FROM Contact ORDER BY LastModifiedDate DESC";

            // 3. Build the URL using UriComponentsBuilder
            // This handles RFC 3986 encoding correctly and avoids double-encoding
            String url = UriComponentsBuilder.fromHttpUrl(instanceUrl)
                    .path("/services/data/" + apiVersion + "/query")
                    .queryParam("q", soql)
                    .build()
                    .toUriString();

            // 4. Execute the request
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(accessToken)),
                    Map.class
            );

            Map<String, Object> body = response.getBody();

            resBody.put("success", true);
            resBody.put("fields", new ArrayList<>(selected));
            resBody.put("totalSize", body != null ? body.get("totalSize") : 0);
            resBody.put("records", body != null ? body.get("records") : Collections.emptyList());
            return resBody;

        } catch (HttpClientErrorException e) {
            // Captures Salesforce API errors (400, 401, etc.)
            resBody.put("success", false);
            resBody.put("status", e.getStatusCode().value());
            resBody.put("error", "Salesforce API Error");
            resBody.put("message", e.getResponseBodyAsString());
            return resBody;

        } catch (Exception e) {
            // Captures generic Java exceptions
            resBody.put("success", false);
            resBody.put("status", 500);
            resBody.put("error", e.getClass().getSimpleName());
            resBody.put("message", e.getMessage());
            return resBody;
        }
    }


    @Override
    public Map<String, Object> getContactDetails(String contactId, String accessToken, String instanceUrl) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Contact/" + contactId;

            ResponseEntity<Map> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(accessToken)),
                    Map.class
            );
            resBody.put("success", true);
            resBody.put("contact", response.getBody());
            return resBody;
        } catch (HttpClientErrorException e) {
            // Salesforce 4xx errors
            resBody.put("success", false);
            resBody.put("status", e.getStatusCode().value());
            resBody.put("error", e.getStatusText());
            resBody.put("message", e.getResponseBodyAsString());
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
    public Map<String, Object> createContact(Map<String, Object> contactData, String accessToken, String instanceUrl) {
        try {
            Map<String, Object> resBody = new HashMap<>();
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Contact/";

            ResponseEntity<Map> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(contactData, authHeaders(accessToken)),
                    Map.class
            );

            boolean success = response.getStatusCode().is2xxSuccessful();
            resBody.put("success", success);
            resBody.put("status", response.getStatusCode().value());

            if (success) {
                resBody.put("contact", response.getBody());
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
    public Map<String, Object> updateContact(String contactId, Map<String, Object> contactData, String accessToken, String instanceUrl) {
        try {
            Map<String, Object> resBody = new HashMap<>();
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Contact/" + contactId;

            ResponseEntity<Void> response = restClient.patch()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .body(contactData)
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
    public Map<String, Object> deleteContact(String contactId, String accessToken, String instanceUrl) {
        try {
            Map<String, Object> resBody = new HashMap<>();
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/Contact/" + contactId;

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
