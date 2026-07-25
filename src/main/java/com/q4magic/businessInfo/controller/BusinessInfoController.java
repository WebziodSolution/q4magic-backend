package com.q4magic.businessInfo.controller;


import com.q4magic.businessInfo.service.BusinessInfoService;
import com.q4magic.common.dto.BusinessInfoDto;
import com.q4magic.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/businessInfo")
public class BusinessInfoController {
    @Autowired
    private BusinessInfoService businessInfoService;

    @GetMapping("/get/{id}")
    public ApiResponse<?> getBusinessInfoId(@PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            BusinessInfoDto businessInfoDto = this.businessInfoService.getBusinessInfoById(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch business info successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch business info", resBody);
        }
    }

    @PostMapping("/create")
    public ApiResponse<?> createBusinessInfo(@RequestBody BusinessInfoDto businessInfoDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            BusinessInfoDto businessInfoDto1 = this.businessInfoService.createBusinessInfo(businessInfoDto);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Business info created successfully", businessInfoDto1);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to create business info", resBody);
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<?> updateBusinessInfo(@PathVariable Integer id, @RequestBody BusinessInfoDto businessInfoDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            BusinessInfoDto businessInfoDto1 = this.businessInfoService.updateBusinessInfo(id,businessInfoDto);
            return new ApiResponse<>(HttpStatus.OK.value(), "Business info updated successfully", businessInfoDto1);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update business info", resBody);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> deleteBusinessInfoId(@PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.businessInfoService.deleteBusinessInfo(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Business info deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete business info", resBody);
        }
    }

    @PostMapping("/uploadBrandLogo")
    public ApiResponse<?> uploadBrandLogo(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestBody Map<String, Object> req) {

        Map<String, Object> resBody = new HashMap<>();
        try {
            String path = this.businessInfoService.uploadBrandLogo(Integer.parseInt(req.get("brandId").toString()), req.get("brandLogo").toString());
            if (path.equals("Error")) {
                return new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Image does not exist in the directory", "");
            }
            return new ApiResponse<>(HttpStatus.OK.value(), "Logo update successfully", path);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update brand logo", resBody);
        }
    }

    @DeleteMapping("/deleteBrandLogo/{brandId}")
    public ApiResponse<?> deleteCompanyLogo(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @PathVariable Integer brandId) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            if (this.businessInfoService.deleteBrandLogo(brandId)) {
                return new ApiResponse<>(HttpStatus.OK.value(), "Logo deleted successfully", "");
            }
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Logo not found", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete brand logo", resBody);
        }
    }
}
