package com.q4magic.docsAttachments.serviceImpl;

import com.q4magic.common.dto.DocsAttachmentsDto;
import com.q4magic.common.models.DocsAttachments;
import com.q4magic.common.models.DocsCategory;
import com.q4magic.common.repository.DocsAttachmentsRepository;
import com.q4magic.common.repository.DocsCategoryRepository;
import com.q4magic.common.service.CommonService;
import com.q4magic.docsAttachments.service.DocsAttachmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service(value = "DocsAttachmentsService")
public class DocsAttachmentsServiceImpl implements DocsAttachmentsService {

    @Value("${360pipeDrive}")
    String FILE_DIRECTORY;

    @Autowired
    private DocsAttachmentsRepository docsAttachmentsRepository;

    @Autowired
    private DocsCategoryRepository docsCategoryRepository;

    @Autowired
    private CommonService commonService;

    @Override
    public List<DocsAttachmentsDto> findByDocsCategory(Integer id) {
        try {
            List<DocsAttachments> docsAttachments = this.docsAttachmentsRepository.findByCategoryId(id);
            List<DocsAttachmentsDto> docsAttachmentsDto = new ArrayList<>();
            if (!docsAttachments.isEmpty()) {
                for (DocsAttachments docsAttachments1 : docsAttachments) {
                    docsAttachmentsDto.add(this.findById(docsAttachments1.getId()));
                }
            }
            return docsAttachmentsDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DocsAttachmentsDto findById(Integer id) {
        try {
            DocsAttachments docsAttachments = this.docsAttachmentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Docs Attachment Not Found"));
            DocsAttachmentsDto docsAttachmentsDto = new DocsAttachmentsDto();
            docsAttachmentsDto.setId(docsAttachments.getId());
            docsAttachmentsDto.setType(docsAttachments.getType());
            docsAttachmentsDto.setCategoryId(docsAttachments.getDocsCategory().getId());
            docsAttachmentsDto.setLinkName(docsAttachments.getLinkName());
            docsAttachmentsDto.setLink(docsAttachments.getLink());
            docsAttachmentsDto.setFileName(docsAttachments.getFileName());
            docsAttachmentsDto.setFileUrl(docsAttachments.getFileUrl());
            return docsAttachmentsDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveAttachments(Integer userId, List<DocsAttachmentsDto> docsAttachmentsDtos) {
        try {
            for (DocsAttachmentsDto docsAttachmentsDto1 : docsAttachmentsDtos) {
                DocsAttachments docsAttachments = new DocsAttachments();
                DocsCategory docsCategory = this.docsCategoryRepository.findById(docsAttachmentsDto1.getCategoryId()).orElseThrow(() -> new RuntimeException("Category Not Found"));
                docsAttachments.setDocsCategory(docsCategory);
                docsAttachments.setType(docsAttachmentsDto1.getType());
                docsAttachments.setLinkName(docsAttachmentsDto1.getLinkName());
                docsAttachments.setLink(docsAttachmentsDto1.getLink());
                docsAttachments.setFileName(docsAttachmentsDto1.getFileName());
                docsAttachments.setFileUrl(docsAttachmentsDto1.getFileUrl());
                docsAttachments.setImageName(docsAttachmentsDto1.getImageName());
                this.docsAttachmentsRepository.save(docsAttachments);
                if (docsAttachments.getType().equals("File")) {
                    String updatedPath = commonService.updateFileLocation(
                            docsAttachmentsDto1.getFileUrl(),
                            userId,
                            "docsAttachments",
                            "docsAttachments/" + docsAttachments.getId()
                    );
                    docsAttachments.setFileUrl(updatedPath);
                }
                this.docsAttachmentsRepository.save(docsAttachments);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateAttachments(Integer userId, Integer id, DocsAttachmentsDto docsAttachmentsDto) {
        try {
            DocsAttachments docsAttachments = this.docsAttachmentsRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Docs Attachment Not Found"));

            DocsCategory docsCategory = this.docsCategoryRepository.findById(docsAttachmentsDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category Not Found"));

            docsAttachments.setDocsCategory(docsCategory);
            docsAttachments.setType(docsAttachmentsDto.getType());
            docsAttachments.setLinkName(docsAttachmentsDto.getLinkName());
            docsAttachments.setLink(docsAttachmentsDto.getLink());
            docsAttachments.setFileName(docsAttachmentsDto.getFileName());
            docsAttachments.setImageName(docsAttachmentsDto.getImageName());

            // ✅ If just updating name, keep existing fileUrl unless a new temp upload comes
            String incomingUrl = docsAttachmentsDto.getFileUrl();

            if ("File".equalsIgnoreCase(docsAttachmentsDto.getType())) {

                // ✅ Move only if the incoming URL is from tempImage
                if (incomingUrl != null && incomingUrl.contains("/tempImage/")) {

                    String updatedPath = commonService.updateFileLocation(
                            incomingUrl,
                            userId,
                            "docsAttachments",
                            "docsAttachments/" + docsAttachments.getId()
                    );

                    docsAttachments.setFileUrl(updatedPath);

                } else {
                    // ✅ already in final folder -> don’t try move again
                    docsAttachments.setFileUrl(incomingUrl != null ? incomingUrl : docsAttachments.getFileUrl());
                }
            } else {
                // link type
                docsAttachments.setFileUrl("");
            }

            this.docsAttachmentsRepository.save(docsAttachments);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAttachments(Integer userId, Integer id) {
        try {
            DocsAttachments docsAttachments = this.docsAttachmentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Docs Attachment Not Found"));
            if (docsAttachments.getType().equals("File")) {
                File existingImagePath = new File(FILE_DIRECTORY + userId + "/docsAttachments/" + docsAttachments.getId() + "/" + docsAttachments.getImageName());
                if (existingImagePath.exists()) {
                    this.commonService.deleteDirectoryRecursively(existingImagePath);
                }
            }
            this.docsAttachmentsRepository.delete(docsAttachments);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAttachmentsFiles(Integer userId, Integer id) {
        try {
            DocsAttachments docsAttachments = this.docsAttachmentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Docs Attachment Not Found"));
            File existingImagePath = new File(FILE_DIRECTORY + userId + "/docsAttachments/" + docsAttachments.getId() + "/" + docsAttachments.getImageName());
            if (existingImagePath.exists()) {
                this.commonService.deleteDirectoryRecursively(existingImagePath);
            }
            docsAttachments.setFileUrl(null);
            docsAttachments.setFileName(null);
            docsAttachments.setImageName(null);
            this.docsAttachmentsRepository.save(docsAttachments);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
