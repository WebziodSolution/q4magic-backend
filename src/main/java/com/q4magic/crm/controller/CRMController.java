package com.q4magic.crm.controller;

import com.q4magic.common.dto.CRMDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.crm.service.CrmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/crm")
public class CRMController {

    @Autowired
    private CrmService crmService;

    @GetMapping("/getAllCRMs")
    public ApiResponse<?> getAllCRMs() {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch crm successfully", this.crmService.getAllCRMs());
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch crm", resBody);
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<?> getCRM(@PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch crm successfully", this.crmService.getCRMById(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch crm", resBody);
        }
    }

    @PostMapping("/create")
    public ApiResponse<?> createCRM(@RequestBody CRMDto crmDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Crm added successfully", this.crmService.createCRM(crmDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to add crm", resBody);
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<?> updateCRM(@PathVariable Integer id, @RequestBody CRMDto crmDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Crm updated successfully", this.crmService.updateCRM(id, crmDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update crm", resBody);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> deleteCRM(@PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.crmService.deleteCRM(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete crm", resBody);
        }
    }
}
