package com.q4magic.products.controller;


import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.ProductsDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.products.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private ProductService productService;

    @GetMapping("/get/all/active")
    public ApiResponse<Map<String, Object>> getALlActiveProduct(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Products fetched successfully", this.productService.getAllActive(userId));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch products", "");
        }
    }

    @GetMapping("/get/all")
    public ApiResponse<Map<String, Object>> getALlProduct(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Products fetched successfully", this.productService.getAllByCustomer(userId));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch products", "");
        }
    }
    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getProduct(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Products fetched successfully", this.productService.getProduct(id));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch products", "");
        }
    }

    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createProduct(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody ProductsDto productsDto) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Products created successfully", this.productService.createProduct(userId, productsDto));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to create products", "");
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> updateProduct(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id, @RequestBody ProductsDto productsDto) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Products update successfully", this.productService.updateProduct(id, productsDto));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update products", "");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteProduct(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        try {
            this.productService.deleteProduct(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Products delete successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete products", "");
        }
    }
}
