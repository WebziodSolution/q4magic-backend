package com.q4magic.common.salesforce.notes.serviceImpl;


import com.q4magic.common.salesforce.notes.service.SalesforceNotesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
public class SalesforceNotesServiceImpl implements SalesforceNotesService {

    @Value("${salesforce.api.version:v60.0}")
    private String apiVersion;

    private HttpHeaders authHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    @Override
    public Map<String, Object> getAllNotesAndAttachments(String accessToken, String instanceUrl, String parentId) {
        Map<String, Object> responseMap = new HashMap<>();
        RestTemplate restTemplate = new RestTemplate();

        try {
            // 🔹 Query Notes
            String notesQuery = "SELECT Id, Title, Body, ParentId, CreatedById, CreatedDate, LastModifiedDate " +
                    "FROM Note WHERE ParentId = '" + parentId + "'";

            String notesUrl = UriComponentsBuilder
                    .fromHttpUrl(instanceUrl + "/services/data/" + apiVersion + "/query")
                    .queryParam("q", notesQuery)
                    .build(false)
                    .toUriString();

            ResponseEntity<Map> notesResponse = restTemplate.exchange(
                    notesUrl, HttpMethod.GET, new HttpEntity<>(authHeaders(accessToken)), Map.class
            );

            // 🔹 Query Attachments
            String attachmentsQuery = "SELECT Id, Name, ContentType, ParentId, CreatedById, CreatedDate, Body " +
                    "FROM Attachment WHERE ParentId = '" + parentId + "'";

            String attachmentsUrl = UriComponentsBuilder
                    .fromHttpUrl(instanceUrl + "/services/data/" + apiVersion + "/query")
                    .queryParam("q", attachmentsQuery)
                    .build(false)
                    .toUriString();

            ResponseEntity<Map> attachmentsResponse = restTemplate.exchange(
                    attachmentsUrl, HttpMethod.GET, new HttpEntity<>(authHeaders(accessToken)), Map.class
            );

            // 🔹 Combine both results
            responseMap.put("success", true);
            responseMap.put("notes", notesResponse.getBody().get("records"));
            responseMap.put("attachments", attachmentsResponse.getBody().get("records"));

        } catch (HttpClientErrorException e) {
            responseMap.put("success", false);
            responseMap.put("status", e.getStatusCode().value());
            responseMap.put("error", e.getStatusText());
            responseMap.put("message", e.getResponseBodyAsString());
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("status", 500);
            responseMap.put("error", e.getClass().getSimpleName());
            responseMap.put("message", e.getMessage());
        }

        return responseMap;
    }
}
