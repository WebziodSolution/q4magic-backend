package com.q4magic.products.service;

import com.q4magic.common.dto.ProductsDto;

import java.util.List;

public interface ProductService {
    ProductsDto getProductBySalesForceId(String id);

    List<ProductsDto> getAllByCustomer(Integer createdBy);

    List<ProductsDto> getAllActive(Integer createdBy);

    ProductsDto getProduct(Integer id);

    ProductsDto createProduct(Integer createdBy, ProductsDto productsDto);

    ProductsDto updateProduct(Integer id, ProductsDto productsDto);

    void deleteProduct(Integer id);
}
