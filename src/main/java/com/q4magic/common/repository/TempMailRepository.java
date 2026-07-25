package com.q4magic.common.repository;

import com.q4magic.common.models.TempMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TempMailRepository extends JpaRepository<TempMail, Integer> {
    @Query("SELECT t FROM TempMail t WHERE t.customers.id = :customerId AND t.isDeleted IS FALSE ORDER BY t.id DESC")
    List<TempMail> findMailsByCustomerId(Integer customerId);

    @Query("SELECT t FROM TempMail t WHERE t.isDeleted IS FALSE AND t.emailScrapingRequests.id = :id ORDER BY t.id DESC")
    List<TempMail> findByRequestId(Integer id);

    @Query("SELECT t FROM TempMail t WHERE t.customers.id=:customerId AND t.email=:email AND t.jobTitle=:jobTitle")
    TempMail findByEmailAndRole(Integer customerId,String email,String jobTitle);
}