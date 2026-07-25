package com.q4magic.common.repository;

import com.q4magic.common.models.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsRepository extends JpaRepository<Products, Integer> {
    @Query(value = "SELECT p FROM Products p WHERE p.customers.id = :id")
    List<Products> getByCustomerId(@Param("id") Integer id);

    @Query(value = "SELECT p FROM Products p WHERE p.isActive = true AND p.customers.id = :id")
    List<Products> getALlActive(@Param("id") Integer id);

    @Query(value = "SELECT p FROM Products p WHERE p.salesforceProductId=:id")
    Products getBySalesForceId(@Param("id") String id);
}