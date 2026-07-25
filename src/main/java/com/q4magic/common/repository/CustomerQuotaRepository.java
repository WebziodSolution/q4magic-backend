package com.q4magic.common.repository;

import com.q4magic.common.models.CustomerQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerQuotaRepository extends JpaRepository<CustomerQuota, Integer> {
    @Query("SELECT c FROM CustomerQuota c WHERE c.customers.id = :id")
    List<CustomerQuota> getALlCustomerQuota(@Param("id") Integer id);
}
