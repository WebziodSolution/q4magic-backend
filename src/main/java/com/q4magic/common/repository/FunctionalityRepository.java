package com.q4magic.common.repository;

import com.q4magic.common.models.Functionality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FunctionalityRepository extends JpaRepository<Functionality, Integer> {
    @Query("SELECT f FROM Functionality f WHERE f.id=:id")
    Functionality findActionById(int id);
}