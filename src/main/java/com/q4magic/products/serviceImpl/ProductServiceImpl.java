package com.q4magic.products.serviceImpl;

import com.q4magic.common.dto.ProductsDto;
import com.q4magic.common.models.Customers;
import com.q4magic.common.models.Products;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.ProductsRepository;
import com.q4magic.products.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "ProductService")
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Override
    public ProductsDto getProductBySalesForceId(String id) {
        try {
            Products products = this.productsRepository.getBySalesForceId(id);
            if (products != null){
                ProductsDto productsDto = new ProductsDto();
                productsDto.setCreatedBy(products.getCustomers().getId());
                BeanUtils.copyProperties(products, productsDto);
                return productsDto;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ProductsDto> getAllByCustomer(Integer createdBy) {
        try {
            List<Products> products = this.productsRepository.getByCustomerId(createdBy);
            List<ProductsDto> productsDtoList = new ArrayList<>();
            if (!products.isEmpty()) {
                for (Products products1 : products) {
                    productsDtoList.add(this.getProduct(products1.getId()));
                }
            }
            return productsDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ProductsDto> getAllActive(Integer createdBy) {
        try {
            List<Products> products = this.productsRepository.getALlActive(createdBy);
            List<ProductsDto> productsDtoList = new ArrayList<>();
            if (!products.isEmpty()) {
                for (Products products1 : products) {
                    productsDtoList.add(this.getProduct(products1.getId()));
                }
            }
            return productsDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProductsDto getProduct(Integer id) {
        try {
            Products products = this.productsRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
            ProductsDto productsDto = new ProductsDto();
            productsDto.setCreatedBy(products.getCustomers().getId());
            BeanUtils.copyProperties(products, productsDto);
            return productsDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProductsDto createProduct(Integer createdBy, ProductsDto productsDto) {
        try {
            Customers customers = this.customersRepository.findById(createdBy).orElseThrow(() -> new RuntimeException("Customer not found"));
            Products products = new Products();
            products.setCustomers(customers);
            BeanUtils.copyProperties(productsDto, products);
            this.productsRepository.save(products);
            return productsDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProductsDto updateProduct(Integer id, ProductsDto productsDto) {
        try {
            Products products = this.productsRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
            BeanUtils.copyProperties(productsDto, products, "customers", "id");
            this.productsRepository.save(products);
            return productsDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteProduct(Integer id) {
        try {
            Products products = this.productsRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
            this.productsRepository.delete(products);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
