package com.q4magic.common.dto;

import lombok.Data;

@Data
public class AuthIDetailsDto {
    private Integer id;
    private Integer documentType;
    private String email;
    private String authAccountNumber;
    private String authOperationId;
    private String authSelfieOperationId;
    private String registeredDate;
}
