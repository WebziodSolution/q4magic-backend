package com.q4magic.common.salesforce.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.models.Customers;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.common.salesforce.opportunities.service.SalesforceOpportunitiesService;
import com.q4magic.common.salesforce.opportunities.service.SalesforceOpportunityProductService;
import kong.unirest.*;
import kong.unirest.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/salesforce")
public class SalesforceController {

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    org.springframework.core.env.Environment env;

    @Value("${salesforce.api.version:v60.0}")
    private String apiVersion;

    @Autowired
    private SalesforceOpportunitiesService salesforceOpportunitiesService;

    @Autowired
    private SalesforceOpportunityProductService salesforceOpportunityProductService;

    @Autowired
    private CustomersRepository customersRepository;

    @GetMapping("/connectToSalesforce")
    public ApiResponse<Map<String, Object>> connectToSalesforce() throws UnsupportedEncodingException {
        Map<String, Object> resBody = new HashMap<>();

        String loginUrl = env.getProperty("sf.login-url");
        String clientId = env.getProperty("sf.client-id");
        String redirectUrl = env.getProperty("sf.redirect-url"); // ✅ FRONTEND redirect only

        StringBuilder url = new StringBuilder();
        url.append(loginUrl)
                .append("/services/oauth2/authorize?response_type=code")
                .append("&client_id=").append(clientId)
                .append("&redirect_uri=").append(redirectUrl);

        resBody.put("url", url.toString());
        return new ApiResponse<>(HttpStatus.OK, "Redirect Url.", resBody);
    }

    /**
     * ✅ NEW: Exchange auth code (received on frontend) for access token.
     */
    @GetMapping("/exchangeToken")
    public ApiResponse<Map<String, Object>> exchangeToken(
            @RequestParam("code") String authCode,
            @RequestParam(value = "code_verifier", required = false) String codeVerifier,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            String loginUrl = env.getProperty("sf.login-url");
            String clientId = env.getProperty("sf.client-id");
            String clientSecret = env.getProperty("sf.client-secret");
            String redirectUrl = env.getProperty("sf.redirect-url"); // ✅ must match frontend redirect exactly
            MultipartBody tokenRequest = Unirest.post(loginUrl + "/services/oauth2/token")
                    .field("code", authCode)
                    .field("grant_type", "authorization_code")
                    .field("client_id", clientId)
                    .field("client_secret", clientSecret)
                    .field("redirect_uri", redirectUrl);
            // ✅ If PKCE code_verifier is present, send it to Salesforce
            if (codeVerifier != null && !codeVerifier.trim().isEmpty()) {
                tokenRequest.field("code_verifier", codeVerifier.trim());
            }
            HttpResponse<JsonNode> tokenResponse = tokenRequest.asJson();
            if (tokenResponse.getStatus() != 200) {
                String errorDetails = tokenResponse.getBody() != null ? tokenResponse.getBody().toString() : tokenResponse.getStatusText();
                return new ApiResponse<>(tokenResponse.getStatus(),
                        "Failed to exchange token: " + errorDetails, null);
            }

            String accessToken = tokenResponse.getBody().getObject().getString("access_token");
            String instanceUrl = tokenResponse.getBody().getObject().getString("instance_url");

            Customers customers = this.customersRepository.findById(userId).orElseThrow(()->new RuntimeException("Customer Not Found"));
            customers.setSalesforceAccessToken(accessToken);
            customers.setSalesforceInstanceUrl(instanceUrl);
            this.customersRepository.save(customers);

            Map<String, Object> data = new HashMap<>();
            data.put("access_token", accessToken);
            data.put("instance_url", instanceUrl);

            Map<String, Object> res = new HashMap<>();
            res.put("data", data);

            return new ApiResponse<>(HttpStatus.OK, "Token exchanged successfully", res);

        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while exchanging token: " + e.getMessage(), null);
        }
    }

    @GetMapping("/userInfo")
    public ApiResponse<Map<String, Object>> getUserInfo(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam("accessToken") String accessToken,
            @RequestParam("instanceUrl") String instanceUrl) {

            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
        try {

            HttpResponse<JsonNode> userResponse = Unirest.get(instanceUrl + "/services/oauth2/userinfo")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .asJson();
            if (userResponse.getStatus() == 200) {
                Map<String, Object> dataMap = userResponse.getBody().getObject().toMap();
                dataMap.put("userId", userId);

                Map<String, Object> result = new HashMap<>();
                result.put("data", dataMap);

                return new ApiResponse<>(HttpStatus.OK, "User info retrieved successfully", result);
            } else {
                Customers customers = this.customersRepository.findById(userId).orElseThrow(()->new RuntimeException("Customer Not Found"));
                customers.setSalesforceAccessToken(null);
                customers.setSalesforceInstanceUrl(null);
                this.customersRepository.save(customers);
                return new ApiResponse<>(userResponse.getStatus(),
                        "Failed to fetch user info: " + userResponse.getStatusText(), null);
            }
        } catch (UnirestException e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error while calling Salesforce userinfo endpoint: " + e.getMessage(), null);
        }
    }
}
