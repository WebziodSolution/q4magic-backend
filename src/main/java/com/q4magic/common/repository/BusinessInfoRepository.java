package com.q4magic.common.repository;

import com.q4magic.common.models.BusinessInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessInfoRepository extends JpaRepository<BusinessInfo, Integer> {
    @Query(value = "SELECT c FROM BusinessInfo c WHERE customers.id = :id")
    BusinessInfo findByCustomerId(@Param("id") Integer id);
}
