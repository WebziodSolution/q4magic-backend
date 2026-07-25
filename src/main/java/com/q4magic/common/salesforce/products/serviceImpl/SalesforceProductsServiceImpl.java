package com.q4magic.common.salesforce.products.serviceImpl;

import com.q4magic.common.salesforce.products.service.SalesforceProductsService;

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

@Service(value = "SalesforceProductsService")
public class SalesforceProductsServiceImpl implements SalesforceProductsService {

    @Autowired
    private RestClient restClient;

    @Autowired
    private org.springframework.core.env.Environment env;

    @Value("${salesforce.api.version:v60.0}")
    private String apiVersion;

    private HttpHeaders authHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Override
    public Map<String, Object> getAllProducts(String accessToken, String instanceUrl) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Build SOQL query to get products with price book entries
            String productsQuery = "SELECT Id, Name, ProductCode, Description, Family, IsActive, " +
                    "(SELECT Id, UnitPrice, Pricebook2Id, Pricebook2.Name, IsActive " +
                    "FROM PricebookEntries WHERE Pricebook2.Name = 'Standard' AND IsActive = true) " +
                    "FROM Product2 WHERE IsActive = true ORDER BY Name";

            String queryUrl = UriComponentsBuilder
                    .fromHttpUrl(instanceUrl + "/services/data/" + apiVersion + "/query")
                    .queryParam("q", productsQuery)
                    .build(false)
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.exchange(
                    queryUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(authHeaders(accessToken)),
                    Map.class
            );

            // Process the response
            List<Map<String, Object>> records = (List<Map<String, Object>>) response.getBody().get("records");
            List<Map<String, Object>> formattedProducts = new ArrayList<>();

            for (Map<String, Object> product : records) {
                Map<String, Object> formattedProduct = new HashMap<>();

                // Basic product info
                formattedProduct.put("Id", product.get("Id"));
                formattedProduct.put("Name", product.get("Name"));
                formattedProduct.put("ProductCode", product.get("ProductCode"));
                formattedProduct.put("Description", product.get("Description"));
                formattedProduct.put("Family", product.get("Family"));
                formattedProduct.put("IsActive", product.get("IsActive"));

                // Extract price from PricebookEntries
                Map<String, Object> pricebookEntries = (Map<String, Object>) product.get("PricebookEntries");
                if (pricebookEntries != null) {
                    List<Map<String, Object>> entries = (List<Map<String, Object>>) pricebookEntries.get("records");
                    if (entries != null && !entries.isEmpty()) {
                        Map<String, Object> pricebookEntry = entries.get(0);
                        formattedProduct.put("PricebookEntryId", pricebookEntry.get("Id"));
                        formattedProduct.put("UnitPrice", pricebookEntry.get("UnitPrice"));
                        formattedProduct.put("PricebookName", "Standard");
                    }
                }

                formattedProducts.add(formattedProduct);
            }

            resBody.put("products", formattedProducts);
            resBody.put("totalSize", formattedProducts.size());
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
}
