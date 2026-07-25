package com.q4magic.docsCategory.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.DocsCategoryDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.docsCategory.service.DocsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/docsCategory")
public class DocsCategoryController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private DocsCategoryService docsCategoryService;

    @GetMapping("/get/all/{id}")
    public ApiResponse<?> getByOppId(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch docs category successfully", this.docsCategoryService.getByOppId(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch docs category", resBody);
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<?> getById(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch docs category successfully", this.docsCategoryService.getById(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch docs category", resBody);
        }
    }

    @PostMapping("/save")
    public ApiResponse<?> save(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody DocsCategoryDto docsCategoryDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Docs category added successfully", this.docsCategoryService.save(docsCategoryDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to add docs category", resBody);
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<?> update(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id, @RequestBody DocsCategoryDto docsCategoryDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Docs category added successfully", this.docsCategoryService.update(id, docsCategoryDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update docs category", resBody);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> delete(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.docsCategoryService.delete(userId,id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Docs category delete successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete docs category", resBody);
        }
    }
}
