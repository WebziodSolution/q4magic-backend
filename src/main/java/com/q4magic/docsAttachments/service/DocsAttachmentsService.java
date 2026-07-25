package com.q4magic.docsAttachments.service;

import com.q4magic.common.dto.DocsAttachmentsDto;

import java.util.List;

public interface DocsAttachmentsService {
    List<DocsAttachmentsDto> findByDocsCategory(Integer id);

    DocsAttachmentsDto findById(Integer id);

    void saveAttachments(Integer userId, List<DocsAttachmentsDto> docsAttachmentsDto);

    void updateAttachments(Integer userId, Integer id, DocsAttachmentsDto docsAttachmentsDto);

    void deleteAttachments(Integer userId, Integer id);

    void deleteAttachmentsFiles(Integer userId, Integer id);

}
