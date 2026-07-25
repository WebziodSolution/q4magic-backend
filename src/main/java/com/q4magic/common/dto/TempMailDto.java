package com.q4magic.common.dto;

import lombok.Data;

@Data
public class TempMailDto {
    private Integer id;
    private Integer requestId;
    private String firstName;
    private String lastName;
    private String email;
    private String companyName;
    private String jobTitle;
    private String address;
    private String website;
    private String phone;
    private Integer createdBy;
    private String createdDate;
}
