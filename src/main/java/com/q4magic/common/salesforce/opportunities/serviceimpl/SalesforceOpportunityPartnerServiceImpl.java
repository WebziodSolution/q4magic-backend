package com.q4magic.common.salesforce.opportunities.serviceimpl;

import com.q4magic.common.dto.OpportunityPartnerDetailsDto;
import com.q4magic.common.salesforce.opportunities.service.SalesforceOpportunityPartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service(value = "SalesforceOpportunityPartnerService")
public class SalesforceOpportunityPartnerServiceImpl implements SalesforceOpportunityPartnerService {

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
    public Map<String, Object> createOpportunityPartner(String accessToken, String instanceUrl, Map<String, Object> partnerData) {
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/OpportunityPartner/";
            ResponseEntity<Map> response = restClient.post()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .body(partnerData)
                    .retrieve()
                    .toEntity(Map.class);

            Map<String, Object> res = new HashMap<>();
            res.put("success", response.getStatusCode().is2xxSuccessful());
            res.put("status", response.getStatusCode().value());
            res.put("data", response.getBody());
            return res;
        } catch (HttpClientErrorException e) {
            return handleError(e);
        } catch (Exception e) {
            return handleGenericError(e);
        }
    }

    @Override
    public Map<String, Object> updateOpportunityPartner(String accessToken, String instanceUrl, String partnerId, Map<String, Object> partnerData) {
        try {

            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/OpportunityPartner/" + partnerId;
            ResponseEntity<Void> response = restClient.patch()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
                    .body(partnerData)
                    .retrieve()
                    .toBodilessEntity();

            Map<String, Object> res = new HashMap<>();
            res.put("success", response.getStatusCode().is2xxSuccessful());
            res.put("status", response.getStatusCode().value());
            res.put("data", response.getBody());
            return res;
        } catch (HttpClientErrorException e) {
            return handleError(e);
        } catch (Exception e) {
            return handleGenericError(e);
        }
    }

    @Override
    public Map<String, Object> deleteOpportunityPartner(String accessToken, String instanceUrl, String partnerId) {
        try {
            String url = instanceUrl + "/services/data/" + apiVersion + "/sobjects/OpportunityPartner/" + partnerId;
            ResponseEntity<Void> response = restClient.delete()
                    .uri(url)
                    .headers(headers -> headers.addAll(authHeaders(accessToken)))
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