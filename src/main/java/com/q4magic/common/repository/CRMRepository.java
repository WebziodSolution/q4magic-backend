package com.q4magic.common.repository;

import com.q4magic.common.models.CRM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CRMRepository extends JpaRepository<CRM, Integer> {
}
