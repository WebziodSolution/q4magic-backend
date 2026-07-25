package com.q4magic.common.dto;

import lombok.Data;

@Data
public class ContactsDto {
    private Integer id;
    private String salesforceAccountId;
    private Integer accountId;
    private String salesforceContactId;
    private String companyName;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private String linkedinProfile;
    private String title;
    private String emailAddress;
    private String role;
    private Integer reportContactId;
    private String notes;
    private String recordStatus;
    private Boolean isDeleted;
    private Integer createdBy;
    private Boolean fromMailScraping;
    private String billDate;
}
