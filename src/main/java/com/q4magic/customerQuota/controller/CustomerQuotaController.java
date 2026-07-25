package com.q4magic.customerQuota.controller;

import com.q4magic.common.dto.CustomerQuotaDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.customerQuota.service.CustomerQuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/customerQuota")
public class CustomerQuotaController {
    @Autowired
    private CustomerQuotaService customerQuotaService;

    @GetMapping("/getAllQuotas/{customerId}")
    public ApiResponse<?> getAllQuotas(@PathVariable Integer customerId) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch quotas successfully", this.customerQuotaService.getAllCustomerQuotas(customerId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch quotas", resBody);
        }
    }

    @GetMapping("/getQuota/{id}")
    public ApiResponse<?> getQuota(@PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Fetch quotas successfully", this.customerQuotaService.getCustomerQuotaById(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch quotas", resBody);
        }
    }

    @PostMapping("/create")
    public ApiResponse<?> createQuota(@RequestBody CustomerQuotaDto customerQuotaDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Quota created successfully", this.customerQuotaService.createCustomerQuota(customerQuotaDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to create quota", resBody);
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<?> updateQuota(@RequestBody CustomerQuotaDto customerQuotaDto,@PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Quota updated successfully", this.customerQuotaService.updateCustomerQuota(id, customerQuotaDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update quota", resBody);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> deleteQuota(@PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.customerQuotaService.deleteCustomerQuota(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Quota deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete quota", resBody);
        }
    }
}
