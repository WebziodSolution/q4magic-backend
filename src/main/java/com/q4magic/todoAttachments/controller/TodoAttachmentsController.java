package com.q4magic.todoAttachments.controller;

import com.q4magic.common.response.ApiResponse;
import com.q4magic.todoAttachments.service.TodoAttachmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/todoAttachments")
public class TodoAttachmentsController {

    @Autowired
    private TodoAttachmentsService todoAttachmentsService;

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> delete(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.todoAttachmentsService.deleteAttachment(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Todos attachment deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete todos attachment", resBody);
        }
    }
}
