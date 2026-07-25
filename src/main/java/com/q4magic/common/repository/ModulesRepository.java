package com.q4magic.common.repository;

import com.q4magic.common.models.Modules;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModulesRepository extends JpaRepository<Modules, Integer> {
    @Query("SELECT m FROM Modules m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :searchKey, '%'))")
    Page<Modules> getModuleByName(@Param("searchKey")String searchKey, Pageable pageable);

    @Query("SELECT r FROM Modules r WHERE r.id=:moduleId")
    Modules findModuleById(Integer moduleId);

    @Query("SELECT r FROM Modules r WHERE r.functionality.id=:id")
    Page<Modules> findModulesByFunctionalityId(Integer id, Pageable pageable);

    @Query("SELECT r FROM Modules r WHERE r.functionality.id=:id")
    List<Modules> findModulesByFunctionalityId(Integer id);

    @Query("SELECT r FROM Modules r WHERE r.functionality.id=:id AND LOWER(r.name) LIKE LOWER(CONCAT('%', :searchKey, '%'))")
    Page<Modules> findModulesByFunctionalityIdAndName(@Param("id")Integer id, @Param("searchKey")String searchKey, Pageable pageable);
}