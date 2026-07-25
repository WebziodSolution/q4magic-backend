package com.q4magic.module.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.ModuleDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.module.service.ModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/module")
public class ModuleController {
    private static final Logger errorLogger = LoggerFactory.getLogger("errorLogger");
    private static final Logger debugLogger = LoggerFactory.getLogger("debugLogger");

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private ModuleService moduleService;

    @PostMapping("/create")
    public ApiResponse<?> createModule(
            @RequestBody ModuleDto moduleDto,
            @RequestHeader(value = "Authorization") String authorizationHeader
    ) {
        Map<String, Object> resBody = new HashMap<>();
        Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
        try {
            resBody.put("module", moduleService.createModule(moduleDto));
            return new ApiResponse<>(HttpStatus.OK.value(), "Module added successfully", resBody);
        } catch (Exception e) {
            errorLogger.error("[ loginUserId : " + loginUserId + " ] createModule Error: " + e);
            debugLogger.debug("[ loginUserId : " + loginUserId + " ] createModule Error: " + e);
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "failed to add new module", resBody);
        }
    }

    @GetMapping("/allModuleListPage")
    public ApiResponse<?> allModuleListPage(
            @RequestParam(value = "searchKey", required = false) String searchKey,
            Pageable pageable,
            @RequestHeader(value = "Authorization") String authorizationHeader
    ) {
        Map<String, Object> resBody = new HashMap<>();
        Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
        try {
            resBody = moduleService.allModuleListPage(searchKey, pageable);
            return new ApiResponse<>(HttpStatus.OK.value(), "Modules list fetched successfully", resBody);
        } catch (Exception e) {
            errorLogger.error("[ loginUserId : " + loginUserId + " ] allModuleListPage Error: " + e);
            debugLogger.debug("[ loginUserId : " + loginUserId + " ] allModuleListPage Error: " + e);
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch modules list", resBody);
        }
    }

    @GetMapping("/moduleByFunctionalityListPage")
    public ApiResponse<?> moduleByFunctionalityListPage(
            @RequestParam(value = "functionalityId") Integer functionalityId,
            @RequestParam(value = "searchKey", required = false) String searchKey,
            Pageable pageable,
            @RequestHeader(value = "Authorization") String authorizationHeader
    ) {
        Map<String, Object> resBody = new HashMap<>();
        Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
        try {
            resBody = moduleService.moduleByFunctionalityListPage(functionalityId, searchKey, pageable);
            return new ApiResponse<>(HttpStatus.OK.value(), "Modules list fetched successfully", resBody);
        } catch (Exception e) {
            errorLogger.error("[ loginUserId : " + loginUserId + " ] moduleByFunctionalityListPage Error: " + e);
            debugLogger.debug("[ loginUserId : " + loginUserId + " ] moduleList Error: " + e);
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch modules list", resBody);
        }
    }

    @GetMapping("/getAllModules")
    public ApiResponse<?> getAllModules(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Map<String, Object> resBody = new HashMap<>();
        Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
        try {
            resBody = moduleService.getAllModules();
            return new ApiResponse<>(HttpStatus.OK.value(), "Modules list fetched successfully", resBody);
        } catch (Exception e) {
            errorLogger.error("[ loginUserId : " + loginUserId + " ] getAllModules Error: " + e);
            debugLogger.debug("[ loginUserId : " + loginUserId + " ] getAllModules Error: " + e);
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch modules list", resBody);
        }
    }

    @GetMapping("/get/{moduleId}")
    public ApiResponse<?> getModuleById(
            @PathVariable Integer moduleId,
            @RequestHeader(value = "Authorization") String authorizationHeader
    ) {
        Map<String, Object> resBody = new HashMap<>();
        Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
        try {
            resBody.put("module", moduleService.getModuleById(moduleId));
            return new ApiResponse<>(HttpStatus.OK.value(), "Module fetched successfully", resBody);
        } catch (Exception e) {
            errorLogger.error("[ loginUserId : " + loginUserId + " ] getModuleById Error: " + e);
            debugLogger.debug("[ loginUserId : " + loginUserId + " ] getModuleById Error: " + e);
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch functionality", resBody);
        }
    }

    @PutMapping("/update/{moduleId}")
    public ApiResponse<?> updateModuleById(
            @PathVariable Integer moduleId,
            @RequestBody ModuleDto moduleDto,
            @RequestHeader(value = "Authorization") String authorizationHeader
    ) {
        Map<String, Object> resBody = new HashMap<>();
        Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
        try {
            resBody.put("module", moduleService.updateModuleById(moduleId, moduleDto));
            return new ApiResponse<>(HttpStatus.OK.value(), "Module updated successfully", resBody);
        } catch (Exception e) {
            errorLogger.error("[ loginUserId : " + loginUserId + " ] updateModuleById Error: " + e);
            debugLogger.debug("[ loginUserId : " + loginUserId + " ] updateModuleById Error: " + e);
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update Module", resBody);
        }
    }

    @DeleteMapping("/delete/{moduleId}")
    public ApiResponse<?> deleteModuleById(
            @PathVariable Integer moduleId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        Map<String, Object> resBody = new HashMap<>();
        Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
        try {
            moduleService.deleteModuleById(moduleId);
            return new ApiResponse<>(HttpStatus.OK.value(), "Module deleted successfully", resBody);
        } catch (Exception e) {
            errorLogger.error("[ loginUserId : " + loginUserId + " ] deleteModuleById Error: " + e);
            debugLogger.debug("[ loginUserId : " + loginUserId + " ] deleteModuleById Error: " + e);
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete module", resBody);
        }
    }
}
