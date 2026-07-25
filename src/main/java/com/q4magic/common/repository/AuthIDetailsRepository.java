package com.q4magic.common.repository;

import com.q4magic.common.models.AuthIDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthIDetailsRepository extends JpaRepository<AuthIDetails, Integer> {
    @Query(value = "SELECT a FROM AuthIDetails a WHERE a.email = :email")
    AuthIDetails findByEmail(@Param("email") String email);
}
