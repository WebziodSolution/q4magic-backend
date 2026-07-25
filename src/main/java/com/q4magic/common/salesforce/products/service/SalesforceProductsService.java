package com.q4magic.common.salesforce.products.service;

import java.util.Map;

public interface SalesforceProductsService {
    Map<String, Object> getAllProducts(String accessToken, String instanceUrl);
}
