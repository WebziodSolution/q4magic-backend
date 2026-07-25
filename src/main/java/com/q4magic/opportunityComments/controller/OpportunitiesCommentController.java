package com.q4magic.opportunityComments.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.OpportunityCommentsDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.opportunityComments.service.OpportunityCommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/opportunitiesComment")
public class OpportunitiesCommentController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private OpportunityCommentsService opportunityCommentsService;

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getOpportunities(@PathVariable("id") Integer id) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Notes fetched successfully", this.opportunityCommentsService.getComments(id));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch notes", "");
        }
    }

    @GetMapping("/get/all/{id}")
    public ApiResponse<Map<String, Object>> getCommentsByOppId(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable("id") Integer id) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Notes fetched successfully", this.opportunityCommentsService.getCommentsByoOppId(id, userId));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch notes", "");
        }
    }

    @PostMapping("/saveComment")
    public ApiResponse<Map<String, Object>> saveComments(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody OpportunityCommentsDto opportunityCommentsDto) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Note saved successfully", this.opportunityCommentsService.saveComments(userId, opportunityCommentsDto));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to save notes", "");
        }
    }

    @PatchMapping("/updateComment/{id}")
    public ApiResponse<Map<String, Object>> updateComments(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody OpportunityCommentsDto opportunityCommentsDto, @PathVariable("id") Integer id) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Note updated successfully", this.opportunityCommentsService.updateComments(id, opportunityCommentsDto));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to updated notes", "");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteComments(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable("id") Integer id) {
        try {
            this.opportunityCommentsService.deleteComments(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Note deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete notes", "");
        }
    }
}
