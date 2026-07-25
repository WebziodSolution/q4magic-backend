package com.q4magic.docsCategory.serviceImpl;

import com.q4magic.common.dto.DocsAttachmentsDto;
import com.q4magic.common.dto.DocsCategoryDto;
import com.q4magic.common.models.DocsCategory;
import com.q4magic.common.models.Opportunities;
import com.q4magic.common.repository.DocsCategoryRepository;
import com.q4magic.common.repository.OpportunitiesRepository;
import com.q4magic.docsAttachments.service.DocsAttachmentsService;
import com.q4magic.docsCategory.service.DocsCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "DocsCategoryService")
public class DocsCategoryServiceImpl implements DocsCategoryService {

    @Autowired
    private DocsCategoryRepository docsCategoryRepository;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private DocsAttachmentsService docsAttachmentsService;

    @Override
    public List<DocsCategoryDto> getByOppId(Integer id) {
        try {
            List<DocsCategory> docsCategories = this.docsCategoryRepository.findByOppId(id);
            List<DocsCategoryDto> docsCategoryDtos = new ArrayList<>();
            if (!docsCategories.isEmpty()) {
                for (DocsCategory docsCategory : docsCategories) {
                    docsCategoryDtos.add(this.getById(docsCategory.getId()));
                }
            }
            return docsCategoryDtos;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DocsCategoryDto getById(Integer id) {
        try {
            DocsCategory docsCategory = this.docsCategoryRepository.findById(id).orElseThrow(() -> new RuntimeException("DocsCategory not found"));
            DocsCategoryDto docsCategoryDto = new DocsCategoryDto();
            docsCategoryDto.setId(docsCategory.getId());
            docsCategoryDto.setOppId(docsCategory.getId());
            docsCategoryDto.setCategoryName(docsCategory.getCategoryName());
            return docsCategoryDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DocsCategoryDto save(DocsCategoryDto docsCategoryDto) {
        try {
            DocsCategory docsCategory = new DocsCategory();
            Opportunities opportunities = this.opportunitiesRepository.findById(docsCategoryDto.getOppId()).orElseThrow(() -> new RuntimeException("Opportunities not found"));
            docsCategory.setOpportunities(opportunities);
            docsCategory.setCategoryName(docsCategoryDto.getCategoryName());
            this.docsCategoryRepository.save(docsCategory);
            docsCategoryDto.setId(docsCategory.getId());
            return docsCategoryDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DocsCategoryDto update(Integer id, DocsCategoryDto docsCategoryDto) {
        try {
            DocsCategory docsCategory = this.docsCategoryRepository.findById(id).orElseThrow(() -> new RuntimeException("DocsCategory not found"));
            Opportunities opportunities = this.opportunitiesRepository.findById(docsCategoryDto.getOppId()).orElseThrow(() -> new RuntimeException("Opportunities not found"));
            docsCategory.setOpportunities(opportunities);
            docsCategory.setCategoryName(docsCategoryDto.getCategoryName());
            this.docsCategoryRepository.save(docsCategory);
            return docsCategoryDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer userId, Integer id) {
        try {
            DocsCategory docsCategory = this.docsCategoryRepository.findById(id).orElseThrow(() -> new RuntimeException("DocsCategory not found"));
            List<DocsAttachmentsDto> docsAttachmentsDtos = this.docsAttachmentsService.findByDocsCategory(docsCategory.getId());
            if (!docsAttachmentsDtos.isEmpty()) {
                for (DocsAttachmentsDto docsAttachmentsDto : docsAttachmentsDtos) {
                    this.docsAttachmentsService.deleteAttachments(userId, docsAttachmentsDto.getId());
                }
            }
            this.docsCategoryRepository.delete(docsCategory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
