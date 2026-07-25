package com.q4magic.common.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.common.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CommonController {
    @Autowired
    private CommonService commonService;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @PostMapping("/uploadFile")
    public ApiResponse<?> uploadFiles(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam String folderName,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) MultipartFile[] files, // For multiple files
            @RequestParam(required = false) MultipartFile file // For a single file
    ) {
        Map<String, Object> resBody = new HashMap<>();
        Integer loginUserId = null;
        try {
            if (userId != null) {
                loginUserId = userId;
            } else {
                loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            // Handle both single and multiple files
            if (file != null) {
                files = new MultipartFile[]{file};
            }

            if (files == null || files.length == 0) {
                return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "No files provided", "");
            }
            Map<String, Object> resBodyObjectMap = commonService.uploadFiles(files, loginUserId, folderName);

            if ((int) resBodyObjectMap.get("status") == 400) {
                return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), resBodyObjectMap.get("message").toString(), "");
            }
            return new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Files uploaded successfully",
                    resBodyObjectMap.get("uploadedFiles")
            );
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/dns/mx")
    public List<String> getMX(@RequestParam String domain) {
        return this.commonService.getDomainMX(domain);
    }
}
