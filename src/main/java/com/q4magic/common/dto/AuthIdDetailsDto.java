package com.q4magic.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class AuthIdDetailsDto {
    private Integer id;
    private String email;
    private Integer documentType;
    private String authAccountNumber;
    private String authOperationId;
    private String authSelfieOperationId;
    private Date addedDate;
}
