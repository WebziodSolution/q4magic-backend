package com.q4magic.contacts.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.ContactsDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.contacts.service.ContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contact")
public class ContactController {

    @Autowired
    private ContactsService contactsService;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @GetMapping("/search")
    public ApiResponse<Map<String, Object>> searchContact(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestParam("query") String query) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Contact fetched successfully", this.contactsService.searchContacts(userId, query));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to create account", "");
        }
    }

    @GetMapping("/getReportHierarch/{id}")
    public ApiResponse<Map<String, Object>> getReportHierarch(@PathVariable("id") Integer contactId) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Report Hierarch fetched successfully", this.contactsService.reportHierarch(contactId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch contact details", "");
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getContact(@PathVariable("id") Integer contactId) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Contact fetched successfully", this.contactsService.getContactById(contactId));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch contact details", "");
        }
    }

    @GetMapping("/get/all")
    public ApiResponse<Map<String, Object>> getAllContact(@RequestHeader(value = "Authorization", required = false) String authorizationHeader, @RequestParam(value = "fetchType", required = false) String fetchType) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Contacts details fetched successfully", this.contactsService.getAllContacts(userId, fetchType));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to fetch contact details", "");
        }
    }

    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createContact(@RequestBody ContactsDto contactDto, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            contactDto.setCreatedBy(userId);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Contact created successfully", this.contactsService.createContact(contactDto, true));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to create account", "");
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> updateContact(@PathVariable("id") Integer id, @RequestBody ContactsDto contactDto) {
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Contact details updated successfully", this.contactsService.updateContact(id, contactDto, true));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to contact fetch details", "");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteContact(@PathVariable("id") Integer id) {
        try {
            this.contactsService.deleteContact(id, true);
            return new ApiResponse<>(HttpStatus.OK.value(), "Contact deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to deleted contact", "");
        }
    }

    @PostMapping("/create/all")
    public ApiResponse<Map<String, Object>> createAllContacts(@RequestBody List<Integer> tempMailIds, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.contactsService.addContacts(userId, tempMailIds);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Contact added successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Fail to added contact", "");
        }
    }

    @PostMapping("/addMultipleContacts")
    public ApiResponse<Map<String, Object>> addMultipleContacts(@RequestBody List<Map<String, Object>> contactList, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            this.contactsService.addMultipleContacts(userId, contactList);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Contact added successfully", "");
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), "");
        }
    }
}
