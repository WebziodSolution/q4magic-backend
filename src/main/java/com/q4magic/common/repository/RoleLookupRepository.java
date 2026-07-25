package com.q4magic.common.repository;

import com.q4magic.common.models.RoleLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface RoleLookupRepository extends JpaRepository<RoleLookup, Integer> {
    @Query(value = "SELECT * FROM role_lookup ORDER BY role_id ASC LIMIT 4", nativeQuery = true)
    List<RoleLookup> findFirstFour();
}
