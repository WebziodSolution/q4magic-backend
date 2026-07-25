package com.q4magic.common.repository;

import com.q4magic.common.models.SubUserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubUserTypeRepository extends JpaRepository<SubUserType, Integer> {
    @Query("SELECT s FROM SubUserType s WHERE s.customers.id = :id")
    List<SubUserType> findCreatedBy(@Param("id") Integer id);

    @Query("SELECT s FROM SubUserType s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :searchKey, '%'))")
    Page<SubUserType> getRolesByName(@Param("searchKey")String searchKey, Pageable pageable);

    @Query("SELECT s FROM SubUserType s WHERE s.customers.id = :id AND LOWER(s.name) = LOWER(:name)")
    SubUserType isExits(@Param("id") Integer createdBy, @Param("name") String name);

    @Query("SELECT s FROM SubUserType s " +
            "WHERE s.customers.id = :customerId " +
            "AND LOWER(s.name) = LOWER(:name) " +
            "AND s.id <> :subUserTypeId")
    SubUserType isNotEqual(@Param("customerId") Integer customerId,
                           @Param("name") String name,
                           @Param("subUserTypeId") Integer subUserTypeId);
}