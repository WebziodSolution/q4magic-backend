package com.q4magic.common.repository;

import com.q4magic.common.models.EmailScrapingRequests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailScrapingRequestsRepository extends JpaRepository<EmailScrapingRequests, Integer> {
    @Query("SELECT e FROM EmailScrapingRequests e WHERE e.customers.id=:customerId")
    List<EmailScrapingRequests> findMailsByCustomerId(Integer customerId);
}