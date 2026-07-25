package com.q4magic.common.repository;

import com.q4magic.common.models.ModuleActions;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleActionsRepository extends JpaRepository<ModuleActions, Integer> {
    @Modifying
    @Transactional
    @Query("DELETE FROM ModuleActions ma WHERE ma.id IN :moduleActionIds")
    void deleteModuleActionByIds(@Param("moduleActionIds") List<Integer> moduleActionIds);

    @Query("SELECT ma FROM ModuleActions ma WHERE ma.module.id = :moduleId AND ma.action.id = :actionId")
    ModuleActions findModuleActionsByModuleAndActions(Integer moduleId, Integer actionId);
}
