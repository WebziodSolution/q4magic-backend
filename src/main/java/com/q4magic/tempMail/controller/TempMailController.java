package com.q4magic.tempMail.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.TempMailDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.tempMail.service.TempMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tempMail")
public class TempMailController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private TempMailService tempMailService;

    @GetMapping("/getAllByGroup")
    public ApiResponse<?> getMailByGroup(@RequestHeader(value = "Authorization") String authorizationHeader) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Mails fetched successfully", this.tempMailService.getMailByGroup(loginUserId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch mails", resBody);
        }
    }

    @GetMapping("/get/all")
    public ApiResponse<?> getAllMails(@RequestHeader(value = "Authorization") String authorizationHeader) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Mails fetched successfully", this.tempMailService.getAllTempMails(loginUserId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch mails", resBody);
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<?> getMails(@RequestHeader(value = "Authorization") String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Mails fetched successfully", this.tempMailService.getTempMailById(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch mails", resBody);
        }
    }

    @PostMapping("/create")
    public ApiResponse<?> createMail(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody List<TempMailDto> tempMailDto) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.tempMailService.createTempMail(tempMailDto,loginUserId);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Mails added successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to add mails", resBody);
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<?> updateMail(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody TempMailDto tempMailDto, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.tempMailService.updateTempMail(id, tempMailDto);
            return new ApiResponse<>(HttpStatus.OK.value(), "Mails updated successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update mails", resBody);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> deleteMail(@RequestHeader(value = "Authorization") String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.tempMailService.deleteTempMail(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Mails updated successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update mails", resBody);
        }
    }

    @DeleteMapping("/deleteInbox/{id}")
    public ApiResponse<?> deleteInbox(@RequestHeader(value = "Authorization") String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.tempMailService.deleteTempMailInbox(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Mails updated successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update mails", resBody);
        }
    }

    @DeleteMapping("/deleteByRequestId/{id}")
    public ApiResponse<?> deleteMailByReqId(@RequestHeader(value = "Authorization") String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.tempMailService.deleteByRequestId(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Mails updated successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update mails", resBody);
        }
    }

    @PostMapping("/delete/all")
    public ApiResponse<?> deleteAllMails(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody List<Integer> ids) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.tempMailService.deleteAllByIds(ids);
            return new ApiResponse<>(HttpStatus.OK.value(), "Mails updated successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update mails", resBody);
        }
    }

    @DeleteMapping("/deleteByRequestIdInbox/{id}")
    public ApiResponse<?> deleteByRequestIdInbox(@RequestHeader(value = "Authorization") String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.tempMailService.deleteIntoMailByRequestId(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Mails updated successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update mails", resBody);
        }
    }

    @PostMapping("/delete/all/inbox")
    public ApiResponse<?> deleteAllMailsInbox(@RequestHeader(value = "Authorization") String authorizationHeader, @RequestBody List<Integer> ids) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.tempMailService.deleteIntoMailAllByIds(ids);
            return new ApiResponse<>(HttpStatus.OK.value(), "Mails updated successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update mails", resBody);
        }
    }
}
