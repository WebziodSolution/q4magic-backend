package com.q4magic.common.repository;

import com.q4magic.common.models.DocsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocsCategoryRepository extends JpaRepository<DocsCategory, Integer> {
    @Query("SELECT o FROM DocsCategory o WHERE o.opportunities.id = :id ORDER BY o.id DESC")
    List<DocsCategory> findByOppId(@Param("id") Integer id);
}
