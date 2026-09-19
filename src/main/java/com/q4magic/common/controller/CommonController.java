package com.q4magic.common.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.outlookCalendar.MicrosoftExchangeRequest;
import com.q4magic.common.dto.outlookCalendar.MicrosoftTokenResponse;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.common.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CommonController {

    @Value("${azure.client-id}")
    private String clientId;

    @Value("${azure.client-secret}")
    private String clientSecret;

    @Value("${azure.tenant-id}")
    private String tenantId;

    @Autowired
    private CommonService commonService;

    @Autowired
    private JwtTokenUtil jwtUtil;

    private final RestTemplate restTemplate = new RestTemplate();

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

    @PostMapping("/microsoft/exchange")
    public ResponseEntity<?> exchangeMicrosoftCode(@RequestBody MicrosoftExchangeRequest request) {
        if (request.getCode() == null || request.getCode().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Authorization code is required"));
        }

        String tokenUrl = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token", tenantId);

        // Microsoft OAuth token endpoint requires application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", request.getCode());
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", request.getRedirectUri());
        body.add("scope", "https://graph.microsoft.com/Mail.Read offline_access openid profile");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<MicrosoftTokenResponse> response = restTemplate.postForEntity(
                    tokenUrl,
                    entity,
                    MicrosoftTokenResponse.class
            );

            MicrosoftTokenResponse tokenData = response.getBody();

            if (tokenData != null && tokenData.getRefreshToken() != null) {
                return ResponseEntity.ok(Map.of(
                        "status", 200,
                        "data", Map.of(
                                "accessToken", tokenData.getAccessToken(),
                                "refreshToken", tokenData.getRefreshToken(),
                                "expiresIn", tokenData.getExpiresIn()
                        )
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", tokenData != null ? tokenData.getErrorDescription() : "Failed to obtain tokens"
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Failed to exchange Microsoft OAuth code",
                    "details", e.getMessage()
            ));
        }
    }
}
