package com.q4magic.common.salesforce.opportunityStage.serviceImpl;

import com.q4magic.common.salesforce.opportunityStage.service.SalesforceOpportunityStageService;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service(value = "SalesforceOpportunityStageService")
public class SalesforceOpportunityStageServiceImpl implements SalesforceOpportunityStageService {

    @Value("${salesforce.api.version:v60.0}")
    private String apiVersion;

    private HttpHeaders authHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Override
    public Map<String, Object> getAllOpportunityStages(String accessToken, String instanceUrl) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1️⃣ Describe the OpportunityStage object to get all field names
            String describeUrl = instanceUrl + "/services/data/" + apiVersion + "/sobjects/OpportunityStage/describe";
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

            // 2️⃣ Build SOQL query to get all OpportunityStage records
            String query = "SELECT " + String.join(",", fieldNames) + " FROM OpportunityStage";

            String queryUrl = UriComponentsBuilder
                    .fromHttpUrl(instanceUrl + "/services/data/" + apiVersion + "/query")
                    .queryParam("q", query)
                    .build(false)
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
}
