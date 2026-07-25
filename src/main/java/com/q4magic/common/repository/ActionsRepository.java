package com.q4magic.common.repository;

import com.q4magic.common.models.Actions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionsRepository extends JpaRepository<Actions, Integer> {
    @Query("SELECT a FROM Actions a WHERE a.id=:actionId")
    Actions findActionById(int actionId);
}