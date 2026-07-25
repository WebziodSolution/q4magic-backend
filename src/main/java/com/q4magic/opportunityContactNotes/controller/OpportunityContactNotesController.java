package com.q4magic.opportunityContactNotes.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.opportunityContactNotes.service.OpportunityContactNotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/opportunitiesContactNotes")
public class OpportunityContactNotesController {

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private OpportunityContactNotesService opportunityContactNoteService;

    @GetMapping("/get/all/{id}")
    public ApiResponse<Map<String, Object>> getAllOpportunitiesContactNotes(@PathVariable("id") Integer id) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities contact notes fetched successfully", this.opportunityContactNoteService.findByOpportunityContactId(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "");
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getOpportunitiesContactNotes(@PathVariable("id") Integer id) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities contact notes fetched successfully", this.opportunityContactNoteService.getById(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteOpportunitiesContactNote(@PathVariable("id") Integer id) {
        try {

            this.opportunityContactNoteService.deleteOppContactNotes(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "");
        }
    }
}
