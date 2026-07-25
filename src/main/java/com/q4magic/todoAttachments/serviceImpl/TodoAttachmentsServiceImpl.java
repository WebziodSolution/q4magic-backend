package com.q4magic.todoAttachments.serviceImpl;

import com.q4magic.common.models.TodoAttachments;
import com.q4magic.common.repository.TodoAttachmentsRepository;
import com.q4magic.common.service.CommonService;
import com.q4magic.todoAttachments.service.TodoAttachmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service(value = "TodoAttachmentsService")
public class TodoAttachmentsServiceImpl implements TodoAttachmentsService {
    @Value("${360pipeDrive}")
    String FILE_DIRECTORY;

    @Autowired
    private TodoAttachmentsRepository todoAttachmentsRepository;

    @Autowired
    private CommonService commonService;

    @Override
    public void deleteAttachment(Integer id) {
        try {
            TodoAttachments todoAttachments = this.todoAttachmentsRepository.findById(id).orElseThrow(() -> new RuntimeException("TodoAttachments not found"));
            if (todoAttachments.getType() != null && todoAttachments.getType().equals("File")) {
                File existingImagePath = new File(FILE_DIRECTORY + todoAttachments.getTodo().getCreatedBy().getId() + "/todo/" + todoAttachments.getTodo().getId());
                if (existingImagePath.exists()) {
                    this.commonService.deleteDirectoryRecursively(existingImagePath);
                    this.todoAttachmentsRepository.delete(todoAttachments);
                }
            } else {
                this.todoAttachmentsRepository.delete(todoAttachments);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}   