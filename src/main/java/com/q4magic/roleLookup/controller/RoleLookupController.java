package com.q4magic.roleLookup.controller;


import com.q4magic.common.dto.RoleLookupDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.roleLookup.serrvice.RoleLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/roles")
public class RoleLookupController {

    @Autowired
    private RoleLookupService roleLookupService;

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getRole(@PathVariable("id") Integer id) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Roles fetched successfully", this.roleLookupService.getRoleById(id));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch roles", "");
        }
    }

    @GetMapping("/get/all")
    public ApiResponse<Map<String, Object>> getAllRoles() {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Roles fetched successfully", this.roleLookupService.getAllRoles());
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch roles", "");
        }
    }

    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createRole(@RequestBody RoleLookupDto roleLookupDto) {
        try {
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Role created successfully", this.roleLookupService.createRole(roleLookupDto));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to create role", "");
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> updateRole(@PathVariable Integer id, @RequestBody RoleLookupDto roleLookupDto) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Role updated successfully", this.roleLookupService.updateRole(id, roleLookupDto));
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to update role", "");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteRole(@PathVariable Integer id) {
        try {
            this.roleLookupService.deleteRole(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Role deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to delete role", "");
        }
    }
}
