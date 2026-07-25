package com.q4magic.docsAttachments.controller;


import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.DocsAttachmentsDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.docsAttachments.service.DocsAttachmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/docsAttachments")
public class DocsAttachmentsController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private DocsAttachmentsService docsAttachmentsService;

    @GetMapping("/get/all/{id}")
    public ApiResponse<?> findByDocsCategory(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch docs attachments successfully", this.docsAttachmentsService.findByDocsCategory(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch docs attachments", resBody);
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<?> findById(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch docs attachments successfully", this.docsAttachmentsService.findById(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch docs attachments", resBody);
        }
    }

    @PostMapping("/save")
    public ApiResponse<?> saveAttachments(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody List<DocsAttachmentsDto> docsAttachmentsDtos) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.docsAttachmentsService.saveAttachments(userId, docsAttachmentsDtos);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Save docs attachments successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to save docs attachments", resBody);
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<?> updateAttachments(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody DocsAttachmentsDto docsAttachmentsDtos, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.docsAttachmentsService.updateAttachments(userId, id, docsAttachmentsDtos);
            return new ApiResponse<>(HttpStatus.OK.value(), "Save docs attachments successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to save docs attachments", resBody);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> deleteAttachments(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.docsAttachmentsService.deleteAttachments(userId, id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Delete docs attachments successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete docs attachments", resBody);
        }
    }

    @DeleteMapping("/deleteAttachmentsFiles/{id}")
    public ApiResponse<?> deleteAttachmentsFiles(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.docsAttachmentsService.deleteAttachmentsFiles(userId, id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Delete docs attachments successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete docs attachments", resBody);
        }
    }
}
