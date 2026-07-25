package com.q4magic.closePlanNotes.controller;

import com.q4magic.closePlanNotes.service.ClosePlanNotesService;
import com.q4magic.common.dto.ClosePlanDto;
import com.q4magic.common.dto.ClosePlanNotesDto;
import com.q4magic.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/closeplannotes")
public class ClosePlanNotesController {
    @Autowired
    private ClosePlanNotesService closePlanNotesService;

    @PostMapping("/saveClosePlanNote")
    public ApiResponse<Map<String, Object>> saveClosePlanNote(@RequestBody ClosePlanNotesDto closePlanNotesDto) {
        try {
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Comment saved successfully", this.closePlanNotesService.saveComment(closePlanNotesDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to save comment", "");
        }
    }

    @GetMapping("/get/all")
    public ApiResponse<?> getAll(@RequestParam Integer closePlanId, @RequestParam Integer contactId) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Comment fetched successfully", this.closePlanNotesService.findByClosePlanIdAndContactId(closePlanId, contactId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch comment", resBody);
        }
    }
}
