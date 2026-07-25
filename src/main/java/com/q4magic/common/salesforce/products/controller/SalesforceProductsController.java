package com.q4magic.common.salesforce.products.controller;

import com.q4magic.common.salesforce.products.service.SalesforceProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/salesforce")
public class SalesforceProductsController {

    @Autowired
    private SalesforceProductsService salesforceProductsService;

    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl) {

        Map<String, Object> result = salesforceProductsService.getAllProducts(
                accessToken.replace("Bearer ", ""),
                instanceUrl
        );

        return ResponseEntity.status((Boolean) result.get("success") ? 200 : 500).body(result);
    }
}