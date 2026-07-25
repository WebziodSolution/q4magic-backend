package com.q4magic.common.repository;

import com.q4magic.common.models.DocsAttachments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocsAttachmentsRepository extends JpaRepository<DocsAttachments, Integer> {
    @Query("SELECT o FROM DocsAttachments o WHERE o.docsCategory.id = :id ORDER BY o.id DESC")
    List<DocsAttachments> findByCategoryId(@Param("id") Integer id);
}
