package com.q4magic.common.dto;

import lombok.Data;

@Data
public class AccountDto {
    private Integer id;
    private Integer crmId;
    private String companyName;
    private String link;
    private String recordStatus;
    private String logo;
    private String salesforceAccountId;
    private String accountName;
    private String phone;
    private Boolean isDeleted;
    private Integer createdBy;
}
