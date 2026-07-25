package com.q4magic.common.repository;


import com.q4magic.common.models.Actions;
import com.q4magic.common.models.RoleModuleActions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RoleModuleActionsRepository extends JpaRepository<RoleModuleActions, Integer> {
    @Query("SELECT rmp.moduleActions.action FROM RoleModuleActions rmp WHERE rmp.role.id = :roleId AND rmp.moduleActions.id IN :moduleActionId")
    List<Actions> findModulesByRoleIdAndMpIds(@Param("roleId") Integer roleId, @Param("moduleActionId") List<Integer> moduleActionId);

    @Query("SELECT rmp FROM RoleModuleActions rmp WHERE rmp.role.id = :roleId")
    List<RoleModuleActions> findByRoleId(@Param("roleId") Integer roleId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RoleModuleActions rmp WHERE rmp.role.id = :roleId")
    void deleteByRoleId(@Param("roleId") Integer roleId);
}
